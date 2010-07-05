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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fakereplace.Agent;
import org.fakereplace.manip.util.MapFunction;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.util.FileReader;

import com.google.common.collect.MapMaker;

/**
 * 
 * This is responsible for scanning for file system changes,
 * and if any are found it hot-replaces the files.
 * 
 * By default it is driven by a thread based scanner, however other scanners can
 * assume responsibility for certain class loaders.
 * 
 * This allows a filter based implementation to scan for changes on every
 * request, while integrations that do not provide a filter can still use the
 * thread based scanner
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class ClassChangeDetector
{

   /**
    * we only want one thread at a time scanning for changes,and in the case of
    * a servlet / filter we don't want them to wait if another request is
    * scanning
    */
   public static final Lock lock = new ReentrantLock();

   /**
    * map of classloaders to root paths
    * some classloaders can have several root paths, e.g. an ear level
    * classloader loading several exploded jar
    */
   static final Map<ClassLoader, Set<File>> classLoaders = new MapMaker().weakKeys().makeMap();

   /**
    * This is a map of all classloaders that should be scanned by the default
    * thread based implementation. this should really be a weak set
    */
   static final Map<ClassLoader, Object> unclaimedClassLoaders = new MapMaker().weakKeys().makeMap();

   /**
    * Map of an object that has claimed a ClassLoader to a ClassLoader.
    */
   static final Map<Object, Map<ClassLoader, Object>> claimedClassLoaders = new MapMaker().weakKeys().makeComputingMap(new MapFunction<Object, ClassLoader, Object>(true));

   static final Map<ClassLoader, Map<File, FileData>> files = new MapMaker().weakKeys().makeMap();

   /**
    * the amount of time to wait between detecting a change and performing the
    * hotswap
    */
   private static final int DELAY_TIME = 300;

   /**
    * adds a class loader to the map of class loaders that are scanned for
    * changes
    * 
    * @param classLoader
    * @param instigatingClassName
    */
   public static void addClassLoader(ClassLoader classLoader, String instigatingClassName)
   {
      try
      {
         lock.lock();
         // this should only be tripped by $Proxy classes, which need to be
         // handled
         // by integrations
         if (classLoader == null || instigatingClassName.contains("$Proxy"))
         {
            return;
         }
         Set<File> roots = classLoaders.get(classLoader);
         if (roots == null)
         {
            roots = new HashSet<File>();
            classLoaders.put(classLoader, roots);
            unclaimedClassLoaders.put(classLoader, new Object());
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
               // if there is a different classloader with the same root remove
               // it
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
      }
      finally
      {
         lock.unlock();
      }
   }

   private static void initRoot(File f, ClassLoader classLoader)
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

   private static void handleInitDirectory(File dir, File rootDir, Map<File, FileData> foundFiles, ClassLoader classLoader)
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

   private static boolean isFileSystemChanged(Set<ClassLoader> loaders)
   {
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {
         Map<File, FileData> fls = new HashMap<File, FileData>();
         if (!loaders.contains(e.getKey()))
         {
            continue;
         }
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

   private static ClassChangeSet getChanges(Set<ClassLoader> loaders)
   {
      ClassChangeSet ret = new ClassChangeSet();
      Map<File, FileData> fls = new HashMap<File, FileData>();
      for (Entry<ClassLoader, Set<File>> e : classLoaders.entrySet())
      {
         if (!loaders.contains(e.getKey()))
         {
            continue;
         }
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
               String d = nf.getFile().getAbsolutePath().substring(nf.getRoot().getAbsolutePath().length() + 1);
               d = d.replace('/', '.');
               d = d.replace('\\', '.');
               d = d.substring(0, d.length() - ".class".length());
               FileInputStream stream = null;
               try
               {
                  stream = new FileInputStream(nf.getFile());
                  byte[] cd = FileReader.readFileBytes(stream);
                  ret.getNewClasses().add(new AddedClass(d, cd, nf.getClassLoader()));
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }
               finally
               {
                  try
                  {
                     stream.close();
                  }
                  catch (IOException e1)
                  {
                     e1.printStackTrace();
                  }
               }

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

   public static void claimClassLoader(Object owner, ClassLoader classLoader)
   {
      claimedClassLoaders.get(owner).put(classLoader, new Object());
      unclaimedClassLoaders.remove(classLoader);
   }

   /**
    * Runs on on any class loaders that have not been 'claimed' by an
    * integration specific scanner
    */
   public static void runDefault()
   {
      run(unclaimedClassLoaders.keySet(), true);
   }

   public static void run(Object owner)
   {
      Map<ClassLoader, Object> aa = claimedClassLoaders.get(owner);
      run(aa.keySet(), false);
   }

   private static void run(Set<ClassLoader> loaders, boolean useDelay)
   {
      if (lock.tryLock())
      {
         try
         {
            if (isFileSystemChanged(loaders))
            {
               // wait for the stuff to be copied
               // we don't want half copied class filed
               if (useDelay)
               {
                  sleep(DELAY_TIME);
               }
               ClassChangeSet changes = getChanges(loaders);
               if (changes.getChangedClasses().isEmpty())
               {
                  return;
               }
               ClassDefinition[] defs = new ClassDefinition[changes.getChangedClasses().size()];
               Class<?>[] changed = new Class[changes.getChangedClasses().size()];
               AddedClass[] newClasses = new AddedClass[changes.getNewClasses().size()];
               int count = 0;
               for (ChangedClassData i : changes.getChangedClasses())
               {
                  System.out.println("REPLACING CLASS: " + i.getJavaClass().getName());
                  changed[count] = i.javaClass;
                  defs[count++] = new ClassDefinition(i.javaClass, i.classFile);
               }
               count = 0;
               for (AddedClass i : changes.getNewClasses())
               {
                  System.out.println("ADDING NEW CLASS: " + i.getClassName());
                  newClasses[count++] = i;
               }
               try
               {
                  Agent.redefine(defs, newClasses);
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
         finally
         {
            lock.unlock();

         }
      }

   }

   protected static void sleep(int millis)
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
