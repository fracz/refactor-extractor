package com.intellij.testFramework;

import com.intellij.ProjectTopics;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionTool;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.idea.IdeaLogger;
import com.intellij.idea.IdeaTestApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.impl.VirtualFilePointerManagerImpl;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFS;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IndexableFileSet;
import com.intellij.util.messages.MessageBusConnection;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A testcase that provides IDEA application and project. Note both are reused for each test run in the session so
 * be careful to return all the modification made to application and project components (such as settings) after
 * test is finished so other test aren't affected. The project is initialized with single module that have single
 * content&amp;source entry. For your convinience the project may be equipped with some mock JDK so your tests may
 * refer to external classes. In order to enable this feature you have to have a folder named "mockJDK" under
 * idea installation home that is used for test running. Place src.zip under that folder. We'd suggest this is real mock
 * so it contains classes that is really needed in order to speed up tests startup.
 */
@SuppressWarnings({"HardCodedStringLiteral"})
@NonNls public class LightIdeaTestCase extends UsefulTestCase implements DataProvider {
  protected static final String PROFILE = "Configurable";
  private static IdeaTestApplication ourApplication;
  private static Project ourProject;
  private static Module ourModule;
  private static Sdk ourJDK;
  private static PsiManager ourPsiManager;
  private static boolean ourAssertionsInTestDetected;
  private static VirtualFile ourSourceRoot;
  private static TestCase ourTestCase = null;
  public static Thread ourTestThread;
  protected Disposable myTestRootDisposable;

  private Map<String, LocalInspectionTool> myAvailableTools = new HashMap<String, LocalInspectionTool>();
  private Map<String, LocalInspectionToolWrapper> myAvailableLocalTools = new HashMap<String, LocalInspectionToolWrapper>();

  /**
   * @return Project to be used in tests for example for project components retrieval.
   */
  public static Project getProject() {
    return ourProject;
  }

  /**
   * @return Module to be used in tests for example for module components retrieval.
   */
  public static Module getModule() {
    return ourModule;
  }

  /**
   * Shortcut to PsiManager.getInstance(getProject())
   */
  public static PsiManager getPsiManager() {
    if (ourPsiManager == null) {
      ourPsiManager = PsiManager.getInstance(ourProject);
    }
    return ourPsiManager;
  }

  public static JavaPsiFacadeEx getJavaFacade() {
    return JavaPsiFacadeEx.getInstanceEx(ourProject);
  }

  public static void initApplication(final DataProvider dataProvider) throws Exception {
    ourApplication = IdeaTestApplication.getInstance();
    ourApplication.setDataProvider(dataProvider);
  }

  private void cleanupApplicationCaches() {
    ((VirtualFilePointerManagerImpl)VirtualFilePointerManager.getInstance()).cleanupForNextTest();
    if (ourProject != null) {
      UndoManager.getInstance(ourProject).dropHistory();
      ((PsiManagerEx)getPsiManager()).getFileManager().cleanupForNextTest();
    }

    resetAllFields();

    /*
    if (ourPsiManager != null) {
      ((PsiManagerImpl)ourPsiManager).cleanupForNextTest();
    }
    */
  }

  protected void resetAllFields() {
    resetClassFields(getClass());
  }

  private void resetClassFields(final Class<?> aClass) {
    if (aClass == null) return;

    final Field[] fields = aClass.getDeclaredFields();
    for (Field field : fields) {
      final int modifiers = field.getModifiers();
      if ((modifiers & Modifier.FINAL) == 0
          &&  (modifiers & Modifier.STATIC) == 0
          && !field.getType().isPrimitive()) {
        field.setAccessible(true);
        try {
          field.set(this, null);
        }
        catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }

    if (aClass == LightIdeaTestCase.class) return;
    resetClassFields(aClass.getSuperclass());
  }

  private static void cleanPersistedVFSContent() {
    ((PersistentFS)ManagingFS.getInstance()).cleanPersistedContents();
  }


  private static void initProject(final Sdk projectJDK) throws Exception {
    final File projectFile = File.createTempFile("temp", ".ipr");
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
      public void run() {
        if (ourProject != null) {
          ProjectUtil.closeProject(ourProject);
        }
        else {
          cleanPersistedVFSContent();
        }

        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectFile);
        ourProject = ProjectManagerEx.getInstanceEx().newProject(projectFile.getPath(), false, false);
        ourPsiManager = null;
        ourModule = createMainModule();


        //ourSourceRoot = DummyFileSystem.getInstance().createRoot("src");

        final VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");

        try {
          ourSourceRoot = dummyRoot.createChildDirectory(this, "src");
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }

        FileBasedIndex.getInstance().registerIndexableSet(new IndexableFileSet() {
          public boolean isInSet(final VirtualFile file) {
            return file.getFileSystem() == ourSourceRoot.getFileSystem();
          }

          public void iterateIndexableFilesIn(final VirtualFile file, final ContentIterator iterator) {
            if (file.isDirectory()) {
              for (VirtualFile child : file.getChildren()) {
                iterateIndexableFilesIn(child, iterator);
              }
            }
            else {
              iterator.processFile(file);
            }
          }
        });

        final ModuleRootManager rootManager = ModuleRootManager.getInstance(ourModule);

        final ModifiableRootModel rootModel = rootManager.getModifiableModel();

        if (projectJDK != null) {
          ourJDK = projectJDK;
          rootModel.setSdk(projectJDK);
        }

        final ContentEntry contentEntry = rootModel.addContentEntry(ourSourceRoot);
        contentEntry.addSourceFolder(ourSourceRoot, false);

        rootModel.commit();

        final MessageBusConnection connection = ourProject.getMessageBus().connect();
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
          public void beforeRootsChange(ModuleRootEvent event) {
            if (!event.isCausedByFileTypesChange()) {
              fail("Root modification in LightIdeaTestCase is not allowed.");
            }
          }

          public void rootsChanged(ModuleRootEvent event) {}
        });

        connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
          public void moduleAdded(Project project, Module module) {
            fail("Adding modules is not permitted in LightIdeaTestCase.");
          }

          public void beforeModuleRemoved(Project project, Module module) {}
          public void moduleRemoved(Project project, Module module) {}
          public void modulesRenamed(Project project, List<Module> modules) {}
        });


        //((PsiManagerImpl) PsiManager.getInstance(ourProject)).runPreStartupActivity();
        ((StartupManagerImpl)StartupManager.getInstance(getProject())).runStartupActivities();
      }
    });
  }

  protected static Module createMainModule() {
    return ApplicationManager.getApplication().runWriteAction(new Computable<Module>() {
      public Module compute() {
        return ModuleManager.getInstance(ourProject).newModule("light_idea_test_case.iml", StdModuleTypes.JAVA);
      }
    });
  }

  /**
   * @return The only source root
   */
  protected static VirtualFile getSourceRoot() {
    return ourSourceRoot;
  }

  protected void setUp() throws Exception {
    super.setUp();
    myTestRootDisposable = new Disposable() {
      public void dispose() {
      }
    };
    initApplication(this);
    doSetup(getProjectJDK(), configureLocalInspectionTools(), myAvailableTools, myAvailableLocalTools);
  }

  public static void doSetup(final Sdk projectJDK,
                      final LocalInspectionTool[] localInspectionTools,
                      final Map<String, LocalInspectionTool> availableToolsMap,
                      final Map<String, LocalInspectionToolWrapper> availableLocalTools) throws Exception {
    assertNull("Previous test " + ourTestCase + " hasn't called tearDown(). Probably overriden without super call.",
               ourTestCase);
    IdeaLogger.ourErrorsOccurred = null;

    if (ourProject == null || isJDKChanged(projectJDK)) {
      initProject(projectJDK);
    }

    ProjectManagerEx.getInstanceEx().setCurrentTestProject(ourProject);

    for (LocalInspectionTool tool : localInspectionTools) {
      _enableInspectionTool(tool, availableToolsMap, availableLocalTools);
    }

    final InspectionProfileImpl profile = new InspectionProfileImpl("Configurable") {
      public InspectionProfileEntry[] getInspectionTools() {
        if (availableLocalTools != null){
          final Collection<LocalInspectionToolWrapper> tools = availableLocalTools.values();
          return tools.toArray(new LocalInspectionToolWrapper[tools.size()]);
        }
        return new InspectionProfileEntry[0];
      }

      public boolean isToolEnabled(HighlightDisplayKey key) {
        return key != null && availableToolsMap.containsKey(key.toString());
      }

      public HighlightDisplayLevel getErrorLevel(HighlightDisplayKey key) {
        final LocalInspectionTool localInspectionTool = key != null ? availableToolsMap.get(key.toString()) : null;
        return localInspectionTool != null ? localInspectionTool.getDefaultLevel() : HighlightDisplayLevel.WARNING;
      }


      public InspectionTool getInspectionTool(String shortName) {
        if (availableToolsMap.containsKey(shortName)) {
          return new LocalInspectionToolWrapper(availableToolsMap.get(shortName));
        }
        return null;
      }

    };
    final InspectionProfileManager inspectionProfileManager = InspectionProfileManager.getInstance();
    inspectionProfileManager.addProfile(profile);
    inspectionProfileManager.setRootProfile(profile.getName());
    InspectionProjectProfileManager.getInstance(getProject()).updateProfile(profile);

    assertFalse(getPsiManager().isDisposed());

    CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(new CodeStyleSettings());
  }

  protected void enableInspectionTool(LocalInspectionTool tool){
    _enableInspectionTool(tool, myAvailableTools, myAvailableLocalTools);
  }

  private static void _enableInspectionTool(final LocalInspectionTool tool,
                                            final Map<String, LocalInspectionTool> availableToolsMap,
                                            final Map<String, LocalInspectionToolWrapper> availableLocalTools) {
    final String shortName = tool.getShortName();
    final HighlightDisplayKey key = HighlightDisplayKey.find(shortName);
    if (key == null){
      HighlightDisplayKey.register(shortName, tool.getDisplayName(), tool.getID());
    }
    availableToolsMap.put(shortName, tool);
    availableLocalTools.put(shortName, new LocalInspectionToolWrapper(tool));
  }

  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[0];
  }

  protected void tearDown() throws Exception {
    Disposer.dispose(myTestRootDisposable);
    myTestRootDisposable = null;
    doTearDown();
    super.tearDown();
  }

  private static void doPostponedFormatting(final Project project) {
    try {
      CommandProcessor.getInstance().runUndoTransparentAction(new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              PsiDocumentManager.getInstance(project).commitAllDocuments();
              PostprocessReformattingAspect.getInstance(project).doPostponedFormatting();
            }
          });
        }
      });
    }
    catch (Throwable e) {
      // Way to go...
    }
  }

  public static void doTearDown() throws Exception {
    doPostponedFormatting(ourProject);

    InspectionProfileManager.getInstance().deleteProfile(PROFILE);
    CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(null);
    assertNotNull("Application components damaged", ProjectManager.getInstance());
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          final VirtualFile[] children = ourSourceRoot.getChildren();
          for (VirtualFile aChildren : children) {
            aChildren.delete(this);
          }
        }
        catch (IOException e) {
          //noinspection CallToPrintStackTrace
          e.printStackTrace();
        }
        FileDocumentManager manager = FileDocumentManager.getInstance();
        if (manager instanceof FileDocumentManagerImpl) {
          ((FileDocumentManagerImpl)manager).dropAllUnsavedDocuments();
        }
      }
    });
//    final Project[] openProjects = ProjectManagerEx.getInstanceEx().getOpenProjects();
//    assertTrue(Arrays.asList(openProjects).contains(ourProject));
    assertFalse(PsiManager.getInstance(getProject()).isDisposed());
    if (!ourAssertionsInTestDetected) {
      if (IdeaLogger.ourErrorsOccurred != null) {
        throw IdeaLogger.ourErrorsOccurred;
      }
      //assertTrue("Logger errors occurred. ", IdeaLogger.ourErrorsOccurred == null);
    }
    ((PsiDocumentManagerImpl)PsiDocumentManager.getInstance(getProject())).clearUncommitedDocuments();



    UndoManager.getGlobalInstance().dropHistory();

    ProjectManagerEx.getInstanceEx().setCurrentTestProject(null);
    ourApplication.setDataProvider(null);
    ourTestCase = null;
    ((PsiManagerImpl)ourPsiManager).cleanupForNextTest();

    final Editor[] allEditors = EditorFactory.getInstance().getAllEditors();
    if (allEditors.length > 0) {
      for (Editor allEditor : allEditors) {
        EditorFactory.getInstance().releaseEditor(allEditor);
      }
      fail("Unreleased editors: " + allEditors.length);
    }
  }

  public final void runBare() throws Throwable {
    final Throwable[] throwables = new Throwable[1];

    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          ourTestThread = Thread.currentThread();
          startRunAndTear();
        }
        catch (Throwable throwable) {
          throwables[0] = throwable;
        }
        finally {
          ourTestThread = null;
          try {
            cleanupApplicationCaches();
          }
          catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
    });

    if (throwables[0] != null) {
      throw throwables[0];
    }

    // just to make sure all deffered Runnable's to finish
    SwingUtilities.invokeAndWait(EmptyRunnable.getInstance());

    if (IdeaLogger.ourErrorsOccurred != null) {
      throw IdeaLogger.ourErrorsOccurred;
    }
  }

  private void startRunAndTear() throws Throwable {
    setUp();
    try {
      ourAssertionsInTestDetected = true;
      runTest();
      ourAssertionsInTestDetected = false;
    }
    finally {
      try{
        tearDown();
      }
      catch(Throwable th){
        //noinspection CallToPrintStackTrace
        th.printStackTrace();
      }
    }
  }

  public Object getData(String dataId) {
    if (dataId.equals(DataConstants.PROJECT)) {
      return ourProject;
    }
    else {
      return null;
    }
  }

  private static boolean isJDKChanged(final Sdk newJDK) {
    return ourJDK == null || newJDK == null || !Comparing.equal(ourJDK.getVersionString(), newJDK.getVersionString());
  }

  protected Sdk getProjectJDK() {
    return JavaSdkImpl.getMockJdk("java 1.4");
  }

  /**
   * Creates dummy source file. One is not placed under source root so some PSI functions like resolve to external classes
   * may not work. Though it works significantly faster and yet can be used if you need to create some PSI structures for
   * test purposes
   *
   * @param fileName - name of the file to create. Extension is used to choose what PSI should be created like java, jsp, aj, xml etc.
   * @param text     - file text.
   * @return dummy psi file.
   * @throws IncorrectOperationException
   */
  protected static PsiFile createFile(String fileName, String text) throws IncorrectOperationException {
    return createPseudoPhysicalFile(fileName, text);
  }

  protected static PsiFile createLightFile(String fileName, String text) throws IncorrectOperationException {
    return PsiFileFactory.getInstance(getProject()).createFileFromText(fileName, FileTypeManager.getInstance().getFileTypeByFileName(
      fileName), text, LocalTimeCounter.currentTime(), false);
  }

  protected static PsiFile createPseudoPhysicalFile(String fileName, String text) throws IncorrectOperationException {
    return PsiFileFactory.getInstance(getProject()).createFileFromText(fileName, FileTypeManager.getInstance().getFileTypeByFileName(
      fileName), text, LocalTimeCounter.currentTime(), true);
  }

  /**
   * Convinient conversion of testSomeTest -> someTest | SomeTest where testSomeTest is the name of current test.
   *
   * @param lowercaseFirstLetter - whether first letter after test should be lowercased.
   */
  protected String getTestName(boolean lowercaseFirstLetter) {
    String name = getName();
    assertTrue("Test name should start with 'test'", name.startsWith("test"));
    name = name.substring("test".length());
    if (lowercaseFirstLetter) {
      name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    return name;
  }

  protected static void commitDocument(final Document document) {
    PsiDocumentManager.getInstance(getProject()).commitDocument(document);
  }
  protected static void commitAllDocuments() {
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
  }

  protected Document getDocument(final PsiFile file) {
    return PsiDocumentManager.getInstance(getProject()).getDocument(file);
  }

  static {
    System.setProperty("jbdt.test.fixture", "com.intellij.designer.dt.IJTestFixture");

    ShutDownTracker.getInstance().registerShutdownThread(0, new Thread(new Runnable() {
      public void run() {
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              if (ourProject != null) {
                final File projectFile = VfsUtil.virtualToIoFile(ourProject.getProjectFile());
                ProjectUtil.closeProject(ourProject);
                FileUtil.delete(projectFile);
              }
            }
          });
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
        catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }));
  }
}