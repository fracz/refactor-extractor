package org.jetbrains.jet;

import com.intellij.codeInsight.daemon.LightDaemonAnalyzerTestCase;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author abreslav
 */
public abstract class JetTestCaseBase extends LightDaemonAnalyzerTestCase {

    private static FilenameFilter emptyFilter;
    private boolean checkInfos = false;
    private String dataPath;
    protected final String name;

    public JetTestCaseBase(String dataPath, String name) {
        this.dataPath = dataPath;
        this.name = name;
    }

    public final JetTestCaseBase setCheckInfos(boolean checkInfos) {
        this.checkInfos = checkInfos;
        return this;
    }

    public static Sdk jdkFromIdeaHome() {
        return new JavaSdkImpl().createJdk("JDK", "compiler/testData/mockJDK-1.7/jre", true);
    }

    @Override
    protected String getTestDataPath() {
        return getTestDataPathBase();
    }

    public static String getTestDataPathBase() {
        return getHomeDirectory() + "/compiler/testData";
    }

    public static String getHomeDirectory() {
       return new File(PathManager.getResourceRoot(JetTestCaseBase.class, "/org/jetbrains/jet/JetTestCaseBase.class")).getParentFile().getParentFile().getParent();
    }

    @Override
    protected Sdk getProjectJDK() {
        return jdkFromIdeaHome();
    }

    @Override
    public String getName() {
        return "test" + name;
    }

    @Override
    protected void runTest() throws Throwable {
        doTest(getTestFilePath(), true, checkInfos);
    }

    @NotNull
    protected String getTestFilePath() {
        return dataPath + File.separator + name + ".jet";
    }

    protected String getDataPath() {
        return dataPath;
    }

    protected void setUp() throws Exception {
        super.setUp();
        emptyFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return true;
            }
        };
    }

    public interface NamedTestFactory {
        @NotNull Test createTest(@NotNull String dataPath, @NotNull String name);
    }

    @NotNull
    public static TestSuite suiteForDirectory(String baseDataDir, @NotNull final String dataPath, boolean recursive, @NotNull NamedTestFactory factory) {
        return suiteForDirectory(baseDataDir, dataPath, recursive, emptyFilter, factory);
    }

    @NotNull
    public static TestSuite suiteForDirectory(String baseDataDir, @NotNull final String dataPath, boolean recursive, final FilenameFilter filter, @NotNull NamedTestFactory factory) {
        TestSuite suite = new TestSuite(dataPath);
        final String extensionJet = ".jet";
        final String extensionKt = ".kt";
        final FilenameFilter extensionFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extensionJet) || name.endsWith(extensionKt);
            }
        };
        FilenameFilter resultFilter;
        if (filter != emptyFilter) {
            resultFilter = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if (extensionFilter.accept(file, s) && filter.accept(file, s)) return true;
                    return false;
                }
            };
        }
        else {
            resultFilter = extensionFilter;
        }
        File dir = new File(baseDataDir + dataPath);
        FileFilter dirFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        if (recursive) {
            File[] files = dir.listFiles(dirFilter);
            assert files != null : dir;
            List<File> subdirs = Arrays.asList(files);
            Collections.sort(subdirs);
            for (File subdir : subdirs) {
                suite.addTest(suiteForDirectory(baseDataDir, dataPath + "/" + subdir.getName(), recursive, factory));
            }
        }
        List<File> files = Arrays.asList(dir.listFiles(resultFilter));
        Collections.sort(files);
        for (File file : files) {
            String fileName = file.getName();
            assert fileName != null;
            String extension = fileName.endsWith(extensionJet) ? extensionJet : extensionKt;
            suite.addTest(factory.createTest(dataPath, fileName.substring(0, fileName.length() - extension.length())));
        }
        return suite;
    }
}