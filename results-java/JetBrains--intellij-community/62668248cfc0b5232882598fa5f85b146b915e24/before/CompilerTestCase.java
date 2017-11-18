package com.intellij.compiler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.FileSystemInterface;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.testFramework.ModuleTestCase;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.ui.UIUtil;
import junit.framework.AssertionFailedError;
import org.apache.log4j.Level;
import org.jdom.Document;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jeka
 */
public abstract class CompilerTestCase extends ModuleTestCase {
  private static final int COMPILING_TIMEOUT = 2 * 60 * 1000;
  protected static final String SOURCE = "source";
  protected static final String CLASSES = "classes";
  protected static final String DATA_FILE_NAME = "data.xml";
  private static final long TIMESTAMP_DELTA = 10L;
  private final String myDataRootPath;
  private Semaphore mySemaphore;

  protected VirtualFile mySourceDir;
  protected VirtualFile myClassesDir;
  protected VirtualFile myDataDir;
  protected VirtualFile myOriginalSourceDir; // directory under testData/compiler/<group>/<testCase>/ where Java sources are located
  private CompilerTestData myData;

  protected final Set<String> myDeletedPaths = new HashSet<String>();
  protected final Set<String> myRecompiledPaths = new HashSet<String>();
  protected VirtualFile myModuleRoot;
  private boolean myUsedMakeToCompile = false;

  protected CompilerTestCase(String groupName) {
    myDataRootPath = PathManagerEx.getTestDataPath() + "/compiler/" + groupName;
  }

  private void waitFor() {
    if (!mySemaphore.waitFor(COMPILING_TIMEOUT)) {
      throw new AssertionFailedError("Compilation isn't finished in " + COMPILING_TIMEOUT  + "ms");
    }
  }

  @Override
  protected void setUp() throws Exception {
    //System.out.println("================BEGIN "+getName()+"====================");
    //CompileDriver.ourDebugMode = true;
    //TranslatingCompilerFilesMonitor.ourDebugMode = true;

    mySemaphore = new Semaphore();
    final Exception[] ex = {null};
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          myUsedMakeToCompile = false;
          CompilerTestCase.super.setUp();
          //((StartupManagerImpl)StartupManager.getInstance(myProject)).runStartupActivities();
        }
        catch (Exception e) {
          ex[0] = e;
        }
        catch (Throwable th) {
          ex[0] = new Exception(th);
        }
      }
    });
    if (ex[0] != null) {
      throw ex[0];
    }
    CompilerTestUtil.setupJavacForTests(myProject);
  }

  //------------------------------------------------------------------------------------------

  protected void doTest() throws Exception {
    final String name = getTestName(true);

    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      @Override
      public void run() {
        //long start = System.currentTimeMillis();
        try {
          doSetup(name);
          mySemaphore.down();
          doCompile(new CompileStatusNotification() {
            @Override
            public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
              try {
                assertFalse("Code did not compile!", aborted);
                if (errors > 0) {
                  final CompilerMessage[] messages = compileContext.getMessages(CompilerMessageCategory.ERROR);
                  StringBuilder errorBuilder = new StringBuilder();
                  for(CompilerMessage message: messages) {
                    errorBuilder.append(message.getMessage()).append("\n");
                  }
                  fail("Compiler errors occurred! " + errorBuilder.toString());
                }
              }
              finally {
                mySemaphore.up();
              }
            }
          }, 1);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        //finally {
        //  long stop = System.currentTimeMillis();
        //  System.out.println("FirstRunnable time:"+(((double)stop-(double)start)/1000.0) + "seconds");
        //}
      }
    }, ModalityState.NON_MODAL);

    waitFor();
    Thread.sleep(5);

    //System.out.println("\n\n=====================SECOND PASS===============================\n\n");
    final AtomicBoolean upToDateStatus = new AtomicBoolean(false);

    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      @Override
      public void run() {
        //long start = System.currentTimeMillis();
        try {
          final Exception[] ex = {null};
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              try {
                copyTestProjectFiles(new NewFilesFilter());
              }
              catch (Exception e) {
                ex[0] = e;
              }
            }
          });
          if (ex[0] != null) {
            throw ex[0];
          }
          mySemaphore.down();

          final CompilerManager compilerManager = CompilerManager.getInstance(myProject);
          upToDateStatus.set(compilerManager.isUpToDate(compilerManager.createProjectCompileScope(myProject)));

          doCompile(new CompileStatusNotification() {
            @Override
            public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
              try {
                String prefix = FileUtil.toSystemIndependentName(myModuleRoot.getPath());
                if (!StringUtil.endsWithChar(prefix, '/')) {
                  prefix += "/";
                }
                for (String p : CompilerManagerImpl.getPathsToDelete()) {
                  String path = FileUtil.toSystemIndependentName(p);
                  if (FileUtil.startsWith(path, prefix)) {
                    path = path.substring(prefix.length());
                  }
                  myDeletedPaths.add(path);
                }

                for (String p : getCompiledPathsToCheck()) {
                  String path = FileUtil.toSystemIndependentName(p);
                  if (FileUtil.startsWith(path, prefix)) {
                    path = path.substring(prefix.length());
                  }
                  myRecompiledPaths.add(path);
                }
              }
              finally {
                mySemaphore.up();
              }
            }
          }, 2);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        //finally {
        //  long stop = System.currentTimeMillis();
        //  System.out.println("FirstRunnable time:"+(((double)stop-(double)start)/1000.0) + "seconds");
        //}
      }
    }, ModalityState.NON_MODAL);

    waitFor();
    checkResults(upToDateStatus.get());
  }

  protected void copyTestProjectFiles(VirtualFileFilter filter) throws Exception {
    copyFiles(myOriginalSourceDir, mySourceDir, filter);
  }

  protected void createTestProjectStructure(VirtualFile moduleRoot) throws Exception {
    copyTestProjectFiles(new JavaFilesFilter());
  }


  protected String[] getCompiledPathsToCheck() {
    return CompilerManagerImpl.getPathsToRecompile();
  }

  // override this in order to change the compilation kind
  protected void doCompile(final CompileStatusNotification notification, int pass) {
    myUsedMakeToCompile = true;
    CompilerManager.getInstance(myProject).make(notification);
  }

  private void checkResults(final boolean upToDateStatus) {

    final String[] deleted = myData.getDeletedByMake();
    final String[] recompiled = myData.getToRecompile();

    if (myUsedMakeToCompile) {
      final boolean expectedUpToDate = deleted.length == 0 && recompiled.length == 0;
      assertEquals("Up-to-date check error: ", expectedUpToDate, upToDateStatus);
    }

    final String deletedPathsString = buildPathsMessage(myDeletedPaths);
    for (final String path : deleted) {
      assertTrue("file \"" + path + "\" should be deleted. | Reported as deleted:" + deletedPathsString, isDeleted(path));
    }
    assertEquals(deleted.length, getDeletedCount());

    final String recompiledpathsString = buildPathsMessage(myRecompiledPaths);
    for (final String path : recompiled) {
      assertTrue("file \"" + path + "\" should be recompiled | Reported as recompiled:" + recompiledpathsString, isRecompiled(path));
    }

    if (recompiled.length != getRecompiledCount()) {
      final Set<String> extraRecompiled = new HashSet<String>();
      extraRecompiled.addAll(myRecompiledPaths);
      extraRecompiled.removeAll(Arrays.asList(recompiled));
      for (String path : extraRecompiled) {
        fail("file \"" + path + "\" should NOT be recompiled");
      }
    }
    //assertEquals(recompiled.length, getRecompiledCount());
  }

  private static String buildPathsMessage(final Collection<String> pathsSet) {
    final StringBuffer message = new StringBuffer();
    for (String p : pathsSet) {
      message.append(" | \"").append(p).append("\"");
    }
    return message.toString();
  }

  public int getDeletedCount() {
    return myDeletedPaths.size();
  }

  public int getRecompiledCount() {
    return myRecompiledPaths.size();
  }

  public boolean isDeleted(String path) {
    return myDeletedPaths.contains(path);
  }

  public boolean isRecompiled(String path) {
    return myRecompiledPaths.contains(path);
  }

  protected void doSetup(final String testName) throws Exception {
    Logger.getInstance("#com.intellij.compiler").setLevel(Level.ERROR); // disable debug output from ordinary category

    CompilerManagerImpl.testSetup();

    CompilerWorkspaceConfiguration.getInstance(myProject).CLEAR_OUTPUT_DIRECTORY = true;

    final Exception[] ex = new Exception[1];
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {

        try {
          myDataDir = getDataRootDir(testName);
          myOriginalSourceDir = myDataDir.findFileByRelativePath(getSourceDirRelativePath());

          File dir = createTempDirectory();
          myModuleRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(dir.getCanonicalPath().replace(File.separatorChar, '/'));
          mySourceDir = createSourcesDir();
          myClassesDir = createOutputDir();

          createTestProjectStructure(myModuleRoot);
          setupMainModuleRootModel();

          final File compilerSystemDir = CompilerPaths.getCompilerSystemDirectory(myProject);
          compilerSystemDir.mkdirs();
          final File[] files = CompilerPaths.getCompilerSystemDirectory().listFiles();
          if (files != null) {
            for (File file : files) {
              if (file.isDirectory()) {
                myFilesToDelete.add(file);
              }
            }
          }

          // load data
          //if (new File(testDataPath).exists()) {
          //  LOG.assertTrue(myDataDir != null, "Path \"" + testDataPath + "\" exists on disk but is not detected by VFS");
          //}
          myData = new CompilerTestData();
          File file = new File(myDataDir.getPath().replace('/', File.separatorChar) + File.separator + DATA_FILE_NAME);
          Document document = JDOMUtil.loadDocument(file);
          myData.readExternal(document.getRootElement());

        }
        catch (Exception e) {
          ex[0] = e;
        }
      }
    });
    CompilerTestUtil.scanSourceRootsToRecompile(myProject);

    if (ex[0] != null) {
      throw ex[0];
    }
  }

  protected VirtualFile createOutputDir() throws IOException {
    return myModuleRoot.createChildDirectory(this, CLASSES);
  }

  protected VirtualFile createSourcesDir() throws IOException {
    return myModuleRoot.createChildDirectory(this, SOURCE);
  }

  protected boolean shouldExcludeOutputFromProject() {
    return true;
  }

  protected void setupMainModuleRootModel() {
    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
    rootModel.clear();

    final VirtualFile libDir = myDataDir.findChild("lib");
    if (libDir != null) {
      if (!libDir.isDirectory()) {
        fail(libDir.getPath() + " is expected to be a directory");
      }
      final VirtualFile[] children = libDir.getChildren();
      final List<VirtualFile> jars = new ArrayList<VirtualFile>();
      for (VirtualFile child : children) {
        if (!child.isDirectory() && (child.getName().endsWith(".jar") || child.getName().endsWith(".zip"))) {
          final String url = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, child.getPath()) + JarFileSystem.JAR_SEPARATOR;
          final VirtualFile jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(url);
          if (jarVirtualFile != null) {
            jars.add(jarVirtualFile);
          }
        }
      }
      if (!jars.isEmpty()) {
        final LibraryTable libraryTable = rootModel.getModuleLibraryTable();
        final Library library = libraryTable.createLibrary("projectlib");
        final Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
        for (final VirtualFile jar : jars) {
          libraryModifiableModel.addRoot(jar, OrderRootType.CLASSES);
        }
        libraryModifiableModel.commit();
      }
    }
    // configure source and output path
    final ContentEntry contentEntry = rootModel.addContentEntry(myModuleRoot);
    contentEntry.addSourceFolder(mySourceDir, false);
    final CompilerModuleExtension compilerModuleExtension = rootModel.getModuleExtension(CompilerModuleExtension.class);
    compilerModuleExtension.setCompilerOutputPath(myClassesDir);
    compilerModuleExtension.inheritCompilerOutputPath(false);
    compilerModuleExtension.setExcludeOutput(shouldExcludeOutputFromProject());

    final Sdk jdk = JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
    rootModel.setSdk(jdk);
    rootModel.commit();
  }

  protected VirtualFile getDataRootDir(final String testName) {
    final String testDataPath = myDataRootPath.replace(File.separatorChar, '/') + "/" + testName;
    return LocalFileSystem.getInstance().refreshAndFindFileByPath(testDataPath);
  }

  protected String getSourceDirRelativePath() {
    return ".";
  }

  protected final void copyFiles(VirtualFile dataDir, VirtualFile destDir, VirtualFileFilter filter) throws Exception {
    final boolean stampsChanged = doCopyFiles(dataDir, destDir, filter);
    if (stampsChanged) {
      // need this to ensure that the compilation start timestamp will be ahead of any stamps of copied files
      Thread.sleep(TIMESTAMP_DELTA);
    }
  }

  private boolean doCopyFiles(VirtualFile dataDir, VirtualFile destDir, VirtualFileFilter filter) throws Exception {
    boolean wereStampChanges = false;
    VirtualFile[] children = destDir.getChildren();
    for (VirtualFile child : children) {
      if (shouldDelete(child)) {
//        System.out.println("{TestCase:} deleted " + child.getPath());
        final String path = child.getPath().replace('/', File.separatorChar);
        child.delete(this);
        assertTrue("File " + path + " should be deleted in order for the test to work properly!", !new File(path).exists());
      }
    }

    children = dataDir.getChildren();
    for (VirtualFile child : children) {

      if (!filter.accept(child)) continue;
      if (child.isDirectory()) { // is a directory
        VirtualFile destChild = destDir.findChild(child.getName());
        if (destChild == null) {
          destChild = destDir.createChildDirectory(this, child.getName());
        }
        wereStampChanges |= doCopyFiles(child, destChild, filter);
      }
      else {
        String name = child.getName();
        long currentTimeStamp = -1;
        if (name.endsWith(".new")) {
          name = name.substring(0, name.length() - ".new".length());
          VirtualFile destChild = destDir.findChild(name);
          if (destChild != null && destChild.isValid()) {
//            System.out.println("Replacing " + destChild.getPath() + " with " + name + "; current timestamp is " + destChild.getPhysicalTimeStamp());
            currentTimeStamp = ((FileSystemInterface)destChild.getFileSystem()).getTimeStamp(destChild);
            final String destChildPath = destChild.getPath().replace('/', File.separatorChar);
            destChild.delete(this);
            assertTrue("File " + destChildPath + " should be deleted in order for the test to work properly!",
                       !new File(destChildPath).exists());
          }
        }

        VirtualFile newChild = VfsUtil.copyFile(this, child, destDir, name);
        //to ensure that compiler will threat the file as changed. On Linux system timestamp may be rounded to multiple of 1000
        final long newStamp = newChild.getTimeStamp();
        if (newStamp == currentTimeStamp) {
          wereStampChanges = true;
          ((NewVirtualFile)newChild).setTimeStamp(newStamp + TIMESTAMP_DELTA);
        }
      }
    }
    return wereStampChanges;
  }

  private boolean shouldDelete(VirtualFile file) {
    String path = VfsUtilCore.getRelativePath(file, myModuleRoot, '/');
    return myData != null && myData.shouldDeletePath(path);
  }

  private static class JavaFilesFilter implements VirtualFileFilter {
    @Override
    public boolean accept(VirtualFile file) {
      return file.isDirectory() || file.getName().endsWith(".java");
    }
  }

  private static class NewFilesFilter implements VirtualFileFilter {
    @Override
    public boolean accept(VirtualFile file) {
      return file.isDirectory() || file.getName().endsWith(".new");
    }
  }

  @Override
  protected void runBareRunnable(Runnable runnable) throws Throwable {
    runnable.run();
  }

  @Override
  protected void tearDown() throws Exception {
    final Exception[] exceptions = {null};
    try {
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        @Override
        public void run() {
          try {
            myDeletedPaths.clear();
            myRecompiledPaths.clear();
            myData = null;
            myClassesDir = null;
            myDataDir = null;
            mySourceDir = null;
            myOriginalSourceDir = null;
            CompilerTestCase.super.tearDown();
          }
          catch (Exception e) {
            exceptions[0] = e;
          }
        }
      }, ModalityState.NON_MODAL);
    }
    finally {
      //System.out.println("================END "+getName()+"====================");
      //CompileDriver.ourDebugMode = false;
      //TranslatingCompilerFilesMonitor.ourDebugMode = false;
    }
    if (exceptions[0] != null) {
      throw exceptions[0];
    }
  }
}