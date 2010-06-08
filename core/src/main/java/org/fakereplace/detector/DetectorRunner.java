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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.fakereplace.Agent;
import org.fakereplace.api.ClassChangeNotifier;

import com.google.common.collect.MapMaker;

/**
 * This class is a massive hack
 * 
 * It scans files for timestamp changes, if any are found it waits a bit
 * and then scans again, and keeps doing that until they stop changing. 
 * 
 * Then it hotswaps all the changed classes 
 * 
 * Ideally it would only be used when running outside a servlet environment
 * and inside servlets a hot deploy filter could be used
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
public class DetectorRunner implements Runnable
{

   static final int POLL_TIME = 2000;

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
   static final Map<ClassLoader, Set<File>> classLoaders = (new MapMaker()).weakKeys().makeMap();

   Map<ClassLoader, Map<File, FileData>> files = (new MapMaker()).weakKeys().makeMap();

   /**
    * adds a class loader to the map of class loaders that are scanned for changes
    * @param classLoader
    * @param instigatingClassName
    */
   public synchronized void addClassLoader(ClassLoader classLoader, String instigatingClassName)
   {
      // this should only be tripped by $Proxy classes
      if (classLoader == null || instigatingClassName.contains("$Proxy"))
      {
         return;
      }
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
            // if there is a different classloader with the same root remove it
            // as that probably means that the app has been undeployed
            System.out.println("ADDING ROOT: " + f.getAbsolutePath() + " to CL " + classLoader);
            Iterator<Entry<ClassLoader, Set<File>>> i = classLoaders.entrySet().iterator();
            while (i.hasNext())
            {
               Entry<ClassLoader, Set<File>> cl = i.next();
               if (cl.getKey() == classLoader)
               {
                  continue;
               }
               if (cl.getValue().contains(f))
               {
                  files.remove(cl.getKey());
                  i.remove();
               }
            }
         }
      }
      else
      {
         System.out.println("ERROR: Could not discover classloader root for classloader of " + instigatingClassName);
      }
   }

   private void initRoot(File f, ClassLoader classLoader)
   {
      if (!files.containsKey(classLoader))
      {
         Map<File, FileData> fileMap = new MapMaker().makeMap();
         handleInitDirectory(f, f, fileMap, classLoader);
         files.put(classLoader, fileMap);
      }
      else
      {
         handleInitDirectory(f, f, files.get(classLoader), classLoader);
      }
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

   private synchronized boolean isFileSystemChanged()
   {
      Map<File, FileData> fls = new HashMap<File, FileData>();
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {
         for (File f : e.getValue())
         {
            handleInitDirectory(f, f, fls, e.getKey());
         }
         Map<File, FileData> oldFileMap = files.get(e.getKey());
         for (File newFile : fls.keySet())
         {
            FileData oldFile = oldFileMap.get(newFile);
            if (oldFile == null)
            {
               // new class file
               return true;
            }
            FileData nf = fls.get(newFile);
            if (nf != null)
            {
               if (nf.getLastModified() > oldFile.getLastModified())
               {
                  return true;
               }
               fls.remove(e);
            }
         }
      }

      return false;
   }

   private synchronized ClassChangeSet getChanges()
   {
      ClassChangeSet ret = new ClassChangeSet();
      Map<File, FileData> fls = new HashMap<File, FileData>();
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {

         for (File f : e.getValue())
         {
            handleInitDirectory(f, f, fls, e.getKey());

         }
         Map<File, FileData> oldFileMap = files.get(e.getKey());
         for (File newFile : fls.keySet())
         {
            FileData oldFile = oldFileMap.get(newFile);
            FileData nf = fls.get(newFile);
            if (oldFile == null)
            {
               // deal with a new file
               String d = nf.file.getAbsolutePath().substring(nf.getRoot().getAbsolutePath().length() + 1);
               d = d.replace('/', '.');
               d = d.replace('\\', '.');
               d = d.substring(0, d.length() - ".class".length());
               ret.getNewClasses().add(new NewClassData(d, nf.getClassLoader()));
               oldFileMap.put(nf.getFile(), nf);

            }
            else if (nf != null)
            {
               if (nf.getLastModified() > oldFile.getLastModified())
               {
                  InputStream in = null;

                  try
                  {
                     in = new FileInputStream(nf.getFile());
                     byte[] data = org.fakereplace.util.FileReader.readFileBytes(in);
                     ret.getChangedClasses().add(new ChangedClassData(nf.getClassLoader().loadClass(nf.getClassName()), data));
                     oldFileMap.put(nf.getFile(), nf);
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
         }
      }

      return ret;
   }

   public void run()
   {
      // no need to do anything for the first 5 seconds
      sleep(5000);
      while (true)
      {
         // wait 2 seconds
         sleep(POLL_TIME);
         try
         {
            if (isFileSystemChanged())
            {
               // wait for the stuff to be copied
               // we don't want half copied class filed
               sleep(DELAY_TIME);
               ClassChangeSet changes = getChanges();
               if (changes.getChangedClasses().isEmpty())
               {
                  continue;
               }
               ClassDefinition[] defs = new ClassDefinition[changes.getChangedClasses().size()];
               Class<?>[] changed = new Class[changes.getChangedClasses().size()];
               Class<?>[] newClasses = new Class[changes.getNewClasses().size()];
               int count = 0;
               for (ChangedClassData i : changes.getChangedClasses())
               {
                  System.out.println("REPLACING CLASS: " + i.getJavaClass().getName());
                  changed[count] = i.javaClass;
                  defs[count++] = new ClassDefinition(i.javaClass, i.classFile);
               }
               count = 0;
               for (NewClassData i : changes.getNewClasses())
               {
                  if (i.getJavaClass() == null)
                  {
                     System.out.println("ADDING NEW CLASS: " + i.getJavaClass().getName());
                     newClasses[count++] = i.getJavaClass();
                  }
               }
               try
               {
                  ClassChangeNotifier.beforeChange(changed, newClasses);
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
         catch (Exception e)
         {
            e.printStackTrace();
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

      final private Long lastModified;
      final private File file, root;
      final private String className;
      final private ClassLoader classLoader;

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

      public Long getLastModified()
      {
         return lastModified;
      }

      public File getFile()
      {
         return file;
      }

      public File getRoot()
      {
         return root;
      }

      public String getClassName()
      {
         return className;
      }

      public ClassLoader getClassLoader()
      {
         return classLoader;
      }
   }

}
