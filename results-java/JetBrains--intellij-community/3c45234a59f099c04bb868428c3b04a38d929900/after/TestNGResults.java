/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Jul 6, 2005
 * Time: 10:49:05 PM
 */
package com.theoryinpractice.testng.ui;

import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.testframework.*;
import com.intellij.execution.testframework.actions.ScrollToTestSourceAction;
import com.intellij.execution.testframework.ui.TestResultsPanel;
import com.intellij.execution.testframework.ui.TestStatusLine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.ClassUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;
import com.theoryinpractice.testng.model.*;
import org.jetbrains.annotations.NonNls;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.TestResultMessage;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestNGResults extends TestResultsPanel implements TestFrameworkRunningModel {
  @NonNls private static final String TESTNG_SPLITTER_PROPERTY = "TestNG.Splitter.Proportion";

  private final TableView resultsTable;

  private final TestNGResultsTableModel model;
  private TestNGTestTreeView tree;

  private final Project project;
  private int count;
  private int total;
  private final Set<TestProxy> failed = new HashSet<TestProxy>();
  private final Map<TestResultMessage, TestProxy> started = new HashMap<TestResultMessage, TestProxy>();
  private TestProxy failedToStart = null;
  private long start;
  private long end;
  private TestTreeBuilder treeBuilder;
  private Animator animator;

  private final Pattern packagePattern = Pattern.compile("(.*)\\.(.*)");
  private final TreeRootNode rootNode;
  private static final String NO_PACKAGE = "No Package";
  private TestNGResults.OpenSourceSelectionListener openSourceListener;
  private final List<ModelListener> myListeners = new ArrayList<ModelListener>();
  private final TestNGConsoleView myConsole;

  public TestNGResults(final JComponent component,
                       final TestNGConfiguration configuration,
                       final TestNGConsoleView console,
                       final RunnerSettings runnerSettings,
                       final ConfigurationPerRunnerSettings configurationSettings) {
    super(component, console.createConsoleActions(), console.getProperties(),
          runnerSettings, configurationSettings, TESTNG_SPLITTER_PROPERTY, 0.5f);
    myConsole = console;
    this.project = configuration.getProject();

    model = new TestNGResultsTableModel();
    resultsTable = new TableView(model);
    resultsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          final Object result = resultsTable.getSelectedObject();
          if (result instanceof TestResultMessage) {
            final String testClass = ((TestResultMessage)result).getTestClass();
            final PsiClass psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), testClass);
            if (psiClass != null) {
              final String method = ((TestResultMessage)result).getMethod();
              if (method != null) {
                final PsiMethod[] psiMethods = psiClass.findMethodsByName(method, false);
                for (PsiMethod psiMethod : psiMethods) {
                  psiMethod.navigate(true);
                  return;
                }
              }
              psiClass.navigate(true);
            }
          }
        }
      }
    });
    rootNode = new TreeRootNode();
  }

  protected JComponent createTestTreeView() {
    tree = new TestNGTestTreeView();

    final TestTreeStructure structure = new TestTreeStructure(project, rootNode);
    tree.attachToModel(this);
    treeBuilder = new TestTreeBuilder(tree, structure);
    Disposer.register(this, treeBuilder);

    animator = new Animator(this, treeBuilder);

    openSourceListener = new OpenSourceSelectionListener(structure, myConsole);
    tree.getSelectionModel().addTreeSelectionListener(openSourceListener);

    return tree;
  }

  @Override
  protected ToolbarPanel createToolbarPanel() {
    return new TestNGToolbarPanel(getProperties(), this, myRunnerSettings, myConfigurationSettings);
  }

  public TestConsoleProperties getProperties() {
    return myProperties;
  }

  protected JComponent createStatisticsPanel() {
    return resultsTable;
  }

  private void updateLabel(TestStatusLine label) {
    StringBuffer sb = new StringBuffer();
    if (end == 0) {
      sb.append("Running: ");
    }
    else {
      sb.append("Done: ");
    }
    sb.append(count).append(" of ").append(total);
    if (failed.size() > 0) sb.append("   Failed: ").append(failed.size()).append(' ');
    if (end != 0) {
      final long time = end - start;
      sb.append(" (").append(time == 0 ? "0.0 s" : NumberFormat.getInstance().format((double)time / 1000.0) + " s").append(")  ");
    }
    label.setText(sb.toString());
  }

  public void testStarted(TestResultMessage result) {
    // TODO This should be an action button which rebuilds the tree when toggled.
    boolean flattenPackages = true;
    TestProxy classNode;
    if (flattenPackages) {
      classNode = getPackageClassNodeFor(result);
    }
    else {
      classNode = getClassNodeFor(result);
    }
    TestProxy proxy = new TestProxy();
    proxy.setParent(classNode);
    proxy.setResultMessage(result);
    started.put(result, proxy);
    animator.setCurrentTestCase(proxy);
    treeBuilder.addItem(classNode, proxy);
    treeBuilder.repaintWithParents(proxy);
    count++;
    if (count > total) total = count;
    if (TestNGConsoleProperties.TRACK_RUNNING_TEST.value(myProperties)) {
      selectTest(proxy);
    }
  }

  public boolean wasTestStarted(TestResultMessage resultMessage) {
    return started.get(resultMessage) != null;
  }

  public void addTestResult(TestResultMessage result, List<Printable> output, int exceptionMark) {
    if (failedToStart != null) {
      output.addAll(failedToStart.getOutput());
      exceptionMark += failedToStart.getExceptionMark();
    }

    TestProxy testCase = started.get(result);
    if (testCase != null) {
      testCase.setResultMessage(result);
      failedToStart = null;
    }
    else {
      //do not remember testresultmessage: test hierarchy is not set
      testCase = new TestProxy();
      failedToStart = testCase;
    }

    testCase.setOutput(output);
    testCase.setExceptionMark(exceptionMark);

    model.addTestResult(result);


    if (result.getResult() == MessageHelper.FAILED_TEST) {
      failed.add(testCase);
      myStatusLine.setStatusColor(ColorProgressBar.RED);
    }
    myStatusLine.setFraction((double)count / total);
    updateLabel(myStatusLine);
  }

  private String packageNameFor(String fqnClassName) {
    Matcher matcher = packagePattern.matcher(fqnClassName);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    else {
      return NO_PACKAGE;
    }
  }

  private String classNameFor(String fqnClassName) {
    Matcher matcher = packagePattern.matcher(fqnClassName);
    if (matcher.matches()) {
      return matcher.group(2);
    }
    else {
      return fqnClassName;
    }
  }

  private TestProxy getPackageClassNodeFor(final TestResultMessage result) {
    TestProxy owner = treeBuilder.getRoot();
    String packageName = packageNameFor(result.getTestClass());
    owner = getChildNodeNamed(owner, packageName);
    if (owner.getPsiElement() == null) {
      owner.setPsiElement(JavaPsiFacade.getInstance(project).findPackage(packageName));
    }
    owner = getChildNodeNamed(owner, classNameFor(result.getTestClass()));
    //look up the psiclass now
    if (owner.getPsiElement() == null) {
      final TestProxy finalOwner = owner;
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          finalOwner.setPsiElement(ClassUtil.findPsiClass(PsiManager.getInstance(project), result.getTestClass()));
        }
      });
    }
    return owner;
  }

  private TestProxy getClassNodeFor(TestResultMessage result) {

    String[] nodes = result.getTestClass().split("\\.");
    TestProxy owner = treeBuilder.getRoot();
    for (String node : nodes) {
      owner = getChildNodeNamed(owner, node);
    }
    return owner;
  }

  private TestProxy getChildNodeNamed(TestProxy currentNode, String node) {
    for (TestProxy child : currentNode.getChildren()) {
      if (child.getName().equals(node)) {
        return child;
      }
    }

    TestProxy child = new TestProxy(node);
    treeBuilder.addItem(currentNode, child);
    return child;
  }

  public void selectTest(TestProxy proxy) {
    if (proxy == null) return;
    DefaultMutableTreeNode node = treeBuilder.ensureTestVisible(proxy);
    if (node == null) return;
    TreePath path = TreeUtil.getPath((TreeNode)tree.getModel().getRoot(), node);
    if (path == null) return;
    tree.setSelectionPath(path);
    tree.makeVisible(path);
    tree.scrollPathToVisible(path);
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public void start() {
    start = System.currentTimeMillis();
    tree.getSelectionModel().setSelectionPath(new TreePath(treeBuilder.getNodeForElement(rootNode)));
    rootNode.setInProgress(true);
    rootNode.setStarted(true);
  }

  public void finish() {
    if (end > 0) return;
    end = System.currentTimeMillis();
    LvcsHelper.addLabel(this);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        animator.stopMovie();
        updateLabel(myStatusLine);
        if (total > count) {
          myStatusLine.setStatusColor(ColorProgressBar.YELLOW);
        }
        rootNode.setInProgress(false);
        if (TestNGConsoleProperties.SELECT_FIRST_DEFECT.value(myProperties)) {
          selectTest(rootNode.getFirstDefect());
        }
        else {
          tree.getSelectionModel().setSelectionPath(new TreePath(treeBuilder.getNodeForElement(rootNode)));
        }
        tree.repaint();
      }
    });
  }

  public void setFilter(final Filter filter) {
    getTreeStructure().setFilter(filter);
    treeBuilder.updateFromRoot();
  }

  public void addListener(ModelListener l) {
    myListeners.add(l);
  }

  public boolean isRunning() {
    return rootNode.isInProgress();
  }

  public TestTreeView getTreeView() {
    return tree;
  }

  public boolean hasTestSuites() {
    return rootNode.getChildren().size() > 0;
  }

  public TestProxy getRoot() {
    return rootNode;
  }

  public void selectAndNotify(final AbstractTestProxy testProxy) {
    selectTest((TestProxy)testProxy);
  }

  public TestTreeStructure getTreeStructure() {
    return (TestTreeStructure)treeBuilder.getTreeStructure();
  }

  public void rebuildTree() {
    treeBuilder.updateFromRoot();
    tree.invalidate();
  }

  public void dispose() {
    for (ModelListener listener : myListeners) {
      listener.onDispose();
    }
    openSourceListener.structure = null;
    openSourceListener.console = null;
    tree.getSelectionModel().removeTreeSelectionListener(openSourceListener);
  }

  public TestProxy getFailedToStart() {
    return failedToStart;
  }

  private class OpenSourceSelectionListener implements TreeSelectionListener {
    private TestTreeStructure structure;
    private TestNGConsoleView console;

    public OpenSourceSelectionListener(TestTreeStructure structure, TestNGConsoleView console) {
      this.structure = structure;
      this.console = console;
    }

    public void valueChanged(TreeSelectionEvent e) {
      TreePath path = e.getPath();
      if (path == null) return;
      TestProxy proxy = (TestProxy)tree.getSelectedTest();
      if (proxy == null) return;
      if (ScrollToTestSourceAction.isScrollEnabled(TestNGResults.this)) {
        OpenSourceUtil.openSourcesFrom(tree, false);
      }
      if (proxy == structure.getRootElement()) {
        console.reset();
      }
      else {
        console
          .setView(proxy.getOutput(), TestNGConsoleProperties.SCROLL_TO_STACK_TRACE.value(getProperties()) ? proxy.getExceptionMark() : 0);
      }
    }
  }
}