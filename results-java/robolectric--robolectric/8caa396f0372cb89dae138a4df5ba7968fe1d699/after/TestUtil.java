package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.RobolectricConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public abstract class TestUtil {
    public static File testDirLocation;

    public static void assertEquals(Collection<?> expected, Collection<?> actual) {
        org.junit.Assert.assertEquals(stringify(expected), stringify(actual));
    }

    public static String stringify(Collection<?> collection) {
        StringBuilder buf = new StringBuilder();
        for (Object o : collection) {
            if (buf.length() > 0) buf.append("\n");
            buf.append(o);
        }
        return buf.toString();
    }

    public static <T> void assertInstanceOf(Class<? extends T> expectedClass, T object) {
        Class actualClass = object.getClass();
        assertTrue(expectedClass + " should be assignable from " + actualClass,
                expectedClass.isAssignableFrom(actualClass));
    }

    public static File file(String... pathParts) {
        return file(new File("."), pathParts);
    }

    public static File file(File f, String... pathParts) {
        for (String pathPart : pathParts) {
            f = new File(f, pathPart);
        }
        return f;
    }

    public static File resourcesBaseDir() {
        if (testDirLocation == null) {
            File testDir = file("src", "test", "resources");
            if (hasTestManifest(testDir)) return testDirLocation = testDir;

            File roboTestDir = file("robolectric", "src", "test", "resources");
            if (hasTestManifest(roboTestDir)) return testDirLocation = roboTestDir;

            throw new RuntimeException("can't find your TestAndroidManifest.xml in "
                    + testDir.getAbsolutePath() + " or " + roboTestDir.getAbsolutePath());
        } else {
            return testDirLocation;
        }
    }

    private static boolean hasTestManifest(File testDir) {
        return new File(testDir, "TestAndroidManifest.xml").isFile();
    }

    public static File resourceFile(String... pathParts) {
        return file(resourcesBaseDir(), pathParts);
    }

    public static RobolectricConfig newConfig(String androidManifestFile) {
        return new RobolectricConfig(resourceFile(androidManifestFile), null, null);
    }

    public static String readString(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}