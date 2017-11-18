package me.tatarka;


import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.tooling.GradleConnector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

import static me.tatarka.TestHelpers.writeBuildFile;
import static me.tatarka.TestHelpers.writeFile;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class AndroidLibPluginTest {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File rootDir;
    private File buildFile;

    @Before
    public void setup() throws Exception {
        rootDir = testProjectDir.getRoot();
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @Test
    public void assembleDebug() throws Exception {
        writeBuildFile(buildFile,
                //language="Groovy"
                "buildscript {\n" +
                        "    repositories {\n" +
                        "        jcenter()\n" +
                        "    }\n" +
                        "    \n" +
                        "    dependencies {\n" +
                        "        classpath files($pluginClasspath)\n" +
                        "        classpath 'com.android.tools.build:gradle:1.4.0-beta6'\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "apply plugin: 'com.android.library'\n" +
                        "apply plugin: 'me.tatarka.retrolambda'\n" +
                        "\n" +
                        "repositories {\n" +
                        "    mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "android {\n" +
                        "    compileSdkVersion 23\n" +
                        "    buildToolsVersion '23.0.1'\n" +
                        "    \n" +
                        "    defaultConfig {\n" +
                        "        minSdkVersion 15\n" +
                        "        targetSdkVersion 23\n" +
                        "    }\n" +
                        "}");

        File manifestFile = new File(rootDir, "src/main/AndroidManifest.xml");

        writeFile(manifestFile,
                //language="XML"
                "<manifest package=\"test\">\n" +
                "    <application/>\n" +
                "</manifest>");

        File javaFile = new File(rootDir, "src/main/java/MainActivity.java");

        writeFile(javaFile, "package test;" +
                "import android.app.Activity;" +
                "import android.os.Bundle;" +
                "import android.util.Log;" +
                "public class MainActivity extends Activity {\n" +
                "    public void onCreate(Bundle savedInstanceState) {\n" +
                "        Runnable lambda = () -> Log.d(\"MainActivity\", \"Hello, Lambda!\");\n" +
                "        lambda.run();\n" +
                "    }\n" +
                "}");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assembleDebug", "--stacktrace")
                .build();

        assertThat(result.getStandardError()).isNullOrEmpty();

        File mainClassFile = new File(rootDir, "build/intermediates/transforms/CLASSES/PROJECT/retrolambda/debug/test/MainActivity.class");
        File lambdaClassFile = new File(rootDir, "build/intermediates/transforms/CLASSES/PROJECT/retrolambda/debug/test/MainActivity$$Lambda$1.class");

        assertThat(mainClassFile).exists();
        assertThat(lambdaClassFile).exists();
    }

    @Test
    public void unitTest() throws Exception {
        writeBuildFile(buildFile,
                //language="Groovy"
                "buildscript {\n" +
                        "    repositories {\n" +
                        "        jcenter()\n" +
                        "    }\n" +
                        "    \n" +
                        "    dependencies {\n" +
                        "        classpath files($pluginClasspath)\n" +
                        "        classpath 'com.android.tools.build:gradle:1.4.0-beta6'\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "apply plugin: 'com.android.library'\n" +
                        "apply plugin: 'me.tatarka.retrolambda'\n" +
                        "\n" +
                        "repositories {\n" +
                        "    mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "android {\n" +
                        "    compileSdkVersion 23\n" +
                        "    buildToolsVersion '23.0.1'\n" +
                        "    \n" +
                        "    defaultConfig {\n" +
                        "        minSdkVersion 15\n" +
                        "        targetSdkVersion 23\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "    testCompile 'junit:junit:4.12'\n" +
                        "}");

        File manifestFile = new File(rootDir, "src/main/AndroidManifest.xml");

        writeFile(manifestFile,
                //language="XML"
                "<manifest package=\"test\">\n" +
                        "    <application/>\n" +
                        "</manifest>");

        File javaFile = new File(rootDir, "src/main/java/Main.java");

        writeFile(javaFile, "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static Callable<String> f() {\n" +
                "        return () -> \"Hello, Lambda Test!\";\n" +
                "    }\n" +
                "}");

        File testJavaFile = new File(rootDir, "src/test/java/Test.java");

        writeFile(testJavaFile, "import org.junit.Assert;\n" +
                "import org.junit.runner.RunWith;\n" +
                "import org.junit.runners.JUnit4;\n" +
                "\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "@RunWith(JUnit4.class)\n" +
                "public class Test {\n" +
                "    @org.junit.Test\n" +
                "    public void test() throws Exception {\n" +
                "        Runnable lambda = () -> Assert.assertTrue(true);\n" +
                "        lambda.run();\n" +
                "        Assert.assertEquals(\"Hello, Lambda Test!\", Main.f().call());\n" +
                "    }\n" +
                "}");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("test", "--stacktrace")
                .build();

        assertThat(result.getStandardError()).isNullOrEmpty();
    }

    @Test
    public void androidTest() throws Exception {
        writeBuildFile(buildFile,
                //language="Groovy"
                "buildscript {\n" +
                        "    repositories {\n" +
                        "        jcenter()\n" +
                        "    }\n" +
                        "    \n" +
                        "    dependencies {\n" +
                        "        classpath files($pluginClasspath)\n" +
                        "        classpath 'com.android.tools.build:gradle:1.4.0-beta6'\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "apply plugin: 'com.android.library'\n" +
                        "apply plugin: 'me.tatarka.retrolambda'\n" +
                        "\n" +
                        "repositories {\n" +
                        "    mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "android {\n" +
                        "    compileSdkVersion 23\n" +
                        "    buildToolsVersion '23.0.1'\n" +
                        "    \n" +
                        "    defaultConfig {\n" +
                        "        minSdkVersion 15\n" +
                        "        targetSdkVersion 23\n" +
                        "        testInstrumentationRunner \"android.support.test.runner.AndroidJUnitRunner\"\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "    androidTestCompile 'com.android.support.test:runner:0.4'\n" +
                        "    androidTestCompile 'com.android.support.test:rules:0.4'\n" +
                        "}");

        File manifestFile = new File(rootDir, "src/main/AndroidManifest.xml");

        writeFile(manifestFile,
                //language="XML"
                "<manifest package=\"test\">\n" +
                        "    <application/>\n" +
                        "</manifest>");

        File javaFile = new File(rootDir, "src/main/java/Main.java");

        writeFile(javaFile, "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static Callable<String> f() {\n" +
                "        return () -> \"Hello, Lambda Test!\";\n" +
                "    }\n" +
                "}");

        File testJavaFile = new File(rootDir, "src/androidTest/java/Test.java");

        writeFile(testJavaFile, "import org.junit.Assert;\n" +
                "import org.junit.runner.RunWith;\n" +
                "import android.support.test.runner.AndroidJUnit4;\n" +
                "\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "@RunWith(AndroidJUnit4.class)\n" +
                "public class Test {\n" +
                "    @org.junit.Test\n" +
                "    public void test() throws Exception {\n" +
                "        Runnable lambda = () -> Assert.assertTrue(true);\n" +
                "        lambda.run();\n" +
                "        Assert.assertEquals(\"Hello, Lambda Test!\", Main.f().call());\n" +
                "    }\n" +
                "}");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("connectedCheck", "--stacktrace")
                .build();

        assertThat(result.getStandardError()).isNullOrEmpty();
    }
}