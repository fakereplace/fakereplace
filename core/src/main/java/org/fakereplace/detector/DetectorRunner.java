package org.fakereplace.detector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.Agent;
import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.util.FileReader;

public class DetectorRunner implements Runnable
{

   static final int POLL_TIME = 1000;

   /**
    * when a change is first detected we wait for DELAY_TIME then
    * scan again, so allow the os to finish copying files.
    */
   static final int DELAY_TIME = 300;

   /**
    * map of classloaders to root paths
    * some classloaders can have several root paths, e.g. an ear level classloader loading
    * several exploded jar
    */
   static Map<ClassLoader, Set<File>> classLoaders = new ConcurrentHashMap<ClassLoader, Set<File>>();

   Map<File, FileData> files = new ConcurrentHashMap<File, FileData>();

   /**
    * adds a class loader to the map of class loaders that are scanned for changes
    * @param classLoader
    * @param instigatingClassName
    */
   public void addClassLoader(ClassLoader classLoader, String instigatingClassName)
   {
      Set<File> roots = classLoaders.get(classLoader);
      if (roots == null)
      {
         roots = new HashSet<File>();
         classLoaders.put(classLoader, roots);
      }

      String resourceName = instigatingClassName.replace('.', '/') + ".class";
      URL url = classLoader.getResource(resourceName);
      if (url == null)
      {
         return;
      }
      String path = url.getPath();
      path = path.substring(0, path.length() - resourceName.length() - 1);
      File f = new File(path);
      if (f.isDirectory())
      {
         // we have the root path
         if (!roots.contains(f))
         {
            initRoot(f, classLoader);
            roots.add(f);
         }
      }
      else
      {
         System.out.println("ERROR: Could not discover classloader root for classloader of " + instigatingClassName);
      }
   }

   private void initRoot(File f, ClassLoader classLoader)
   {
      handleInitDirectory(f, f, files, classLoader);
   }

   private void handleInitDirectory(File dir, File rootDir, Map<File, FileData> foundFiles, ClassLoader classLoader)
   {
      if (!dir.isDirectory())
         return;
      String dirPath = rootDir.getAbsolutePath();
      for (File f : dir.listFiles())
      {
         if (!f.isDirectory())
         {
            if (f.getName().endsWith(".class"))
            {
               String d = f.getAbsolutePath().substring(dirPath.length() + 1);
               d = d.replace('/', '.');
               d = d.replace('\\', '.');
               d = d.substring(0, d.length() - ".class".length());

               FileData fd = new FileData(f.lastModified(), f, rootDir, d, classLoader);
               foundFiles.put(f, fd);

            }
         }
         else
         {
            handleInitDirectory(f, rootDir, foundFiles, classLoader);
         }
      }
   }

   private boolean isFileSystemChanged()
   {
      Map<File, FileData> fls = new HashMap<File, FileData>();
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {
         for (File f : e.getValue())
         {
            handleInitDirectory(f, f, fls, e.getKey());
         }
      }
      for (File e : files.keySet())
      {
         FileData oldFile = files.get(e);
         if (oldFile == null)
         {
            // new class file
            return true;
         }
         FileData nf = fls.get(e);
         if (nf != null)
         {
            if (nf.lastModified > oldFile.lastModified)
            {
               return true;
            }
            fls.remove(e);
         }
         else
         {
            // the file has been removed
            // not much we can do here
         }
      }
      // files have been added
      if (!fls.isEmpty())
      {
         return true;
      }
      return false;
   }

   private ClassChangeSet getChanges()
   {
      ClassChangeSet ret = new ClassChangeSet();
      Map<File, FileData> fls = new HashMap<File, FileData>();
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {
         for (File f : e.getValue())
         {
            handleInitDirectory(f, f, fls, e.getKey());
         }
      }
      for (File e : files.keySet())
      {
         FileData oldFile = files.get(e);
         FileData nf = fls.get(e);
         if (oldFile == null)
         {
            // deal with a new file
            String d = nf.file.getAbsolutePath().substring(nf.root.getAbsolutePath().length() + 1);
            d = d.replace('/', '.');
            d = d.replace('\\', '.');
            d = d.substring(0, d.length() - ".class".length());
            ret.getNewClasses().add(new NewClassData(d, nf.classLoader));
            files.put(nf.file, nf);
         }
         if (nf != null)
         {
            if (nf.lastModified > oldFile.lastModified)
            {
               InputStream in = null;

               try
               {
                  in = new FileInputStream(nf.file);
                  byte[] data = FileReader.readFileBytes(in);
                  ret.getChangedClasses().add(new ChangedClassData(nf.classLoader.loadClass(nf.className), data));
                  files.put(nf.file, nf);
               }
               catch (Exception e1)
               {
                  System.out.println("ERROR reading changed class file " + nf.file + " - " + e1.getMessage());
               }
               finally
               {
                  try
                  {
                     in.close();
                  }
                  catch (IOException e1)
                  {
                  }
               }

            }
            fls.remove(e);
         }
         else
         {
            // the file has been removed
            // not much we can do here
         }
      }
      return ret;
   }

   public void run()
   {
      // no need to do anything for the first 10 seconds
      sleep(10000);
      while (true)
      {
         // wait 2 seconds
         sleep(POLL_TIME);
         if (isFileSystemChanged())
         {
            // wait for the stuff to be copied
            // we don't want half copied class filed
            sleep(DELAY_TIME);
            ClassChangeSet changes = getChanges();
            ClassDefinition[] defs = new ClassDefinition[changes.getChangedClasses().size()];
            Class<?>[] changed = new Class[changes.getChangedClasses().size()];
            Class<?>[] newClasses = changes.newClasses.toArray(new Class[0]);
            int count = 0;
            for (ChangedClassData i : changes.getChangedClasses())
            {
               System.out.println("REPLACING CLASS: " + i.getJavaClass().getName());
               changed[count] = i.javaClass;
               defs[count++] = new ClassDefinition(i.javaClass, i.classFile);
            }
            try
            {
               Agent.redefine(defs);
               ClassChangeNotifier.notify(changed, newClasses);
            }
            catch (UnmodifiableClassException e)
            {
               System.out.println("ERROR REPLACING CLASSES");
               e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
               System.out.println("ERROR REPLACING CLASSES");
               e.printStackTrace();
            }
         }

      }
   }

   public void sleep(int millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {

      }
   }

   static class FileData
   {

      public FileData(Long lastModified, File file, File root, String className, ClassLoader classLoader)
      {
         this.lastModified = lastModified;
         this.file = file;
         this.root = root;
         this.className = className;
         this.classLoader = classLoader;
      }

      final Long lastModified;
      final File file, root;
      final String className;
      final ClassLoader classLoader;

      @Override
      public boolean equals(Object obj)
      {
         return file.equals(((FileData) obj).file);
      }

      @Override
      public int hashCode()
      {
         return file.hashCode();
      }
   }

}
