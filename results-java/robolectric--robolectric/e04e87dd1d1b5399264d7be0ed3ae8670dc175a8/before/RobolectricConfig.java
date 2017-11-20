package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;

import static android.content.pm.ApplicationInfo.*;

public class RobolectricConfig {
    private File androidManifestFile;
    private File resourceDirectory;
    private File assetsDirectory;
    private String rClassName;
    private String packageName;
    private String processName;
    private String applicationName;
    private boolean manifestIsParsed = false;
    private int sdkVersion;
    private int minSdkVersion;
    private int applicationFlags;

	/**
     * Creates a Robolectric configuration using default Android files relative to the specified base directory.
     * <p/>
     * The manifest will be baseDir/AndroidManifest.xml, res will be baseDir/res, and assets in baseDir/assets.
     *
     * @param baseDir the base directory of your Android project
     */
    public RobolectricConfig(File baseDir) {
        this(new File(baseDir, "AndroidManifest.xml"), new File(baseDir, "res"), new File(baseDir, "assets"));
    }

    public RobolectricConfig(File androidManifestFile, File resourceDirectory) {
        this(androidManifestFile, resourceDirectory, new File(resourceDirectory.getParent(), "assets"));
    }

    /**
     * Creates a Robolectric configuration using specified locations.
     *
     * @param androidManifestFile location of the AndroidManifest.xml file
     * @param resourceDirectory   location of the res directory
     * @param assetsDirectory     location of the assets directory
     */
    public RobolectricConfig(File androidManifestFile, File resourceDirectory, File assetsDirectory) {
        this.androidManifestFile = androidManifestFile;
        this.resourceDirectory = resourceDirectory;
        this.assetsDirectory = assetsDirectory;
    }

    public String getRClassName() throws Exception {
        parseAndroidManifest();
        return rClassName;
    }

    public void validate() throws FileNotFoundException {
        if (!androidManifestFile.exists() || !androidManifestFile.isFile()) {
            throw new FileNotFoundException(androidManifestFile.getAbsolutePath() + " not found or not a file; it should point to your project's AndroidManifest.xml");
        }

        if (!getResourceDirectory().exists() || !getResourceDirectory().isDirectory()) {
            throw new FileNotFoundException(getResourceDirectory().getAbsolutePath() + " not found or not a directory; it should point to your project's res directory");
        }
    }

    private void parseAndroidManifest() {
        if (manifestIsParsed) {
            return;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document manifestDocument = db.parse(androidManifestFile);

            packageName = getTagAttributeText(manifestDocument, "manifest", "package");
            rClassName = packageName + ".R";
            applicationName = getTagAttributeText(manifestDocument, "application", "android:name");
            minSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion", 9);
            sdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion", 9);

            processName = getTagAttributeText(manifestDocument, "application", "android:process");
            if (processName == null) {
            	processName = packageName;
            }

            parseApplicationFlags(manifestDocument);

        } catch (Exception ignored) {
        }
        manifestIsParsed = true;
    }

    private void parseApplicationFlags(Document manifestDocument) {
        applicationFlags = getApplicationFlag(manifestDocument, "android:allowBackup", FLAG_ALLOW_BACKUP);
        applicationFlags += getApplicationFlag(manifestDocument, "android:allowClearUserData", FLAG_ALLOW_CLEAR_USER_DATA);
        applicationFlags += getApplicationFlag(manifestDocument, "android:allowTaskReparenting", FLAG_ALLOW_TASK_REPARENTING);
        applicationFlags += getApplicationFlag(manifestDocument, "android:debuggable", FLAG_DEBUGGABLE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:hasCode", FLAG_HAS_CODE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:killAfterRestore", FLAG_KILL_AFTER_RESTORE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:persistent", FLAG_PERSISTENT);
        applicationFlags += getApplicationFlag(manifestDocument, "android:resizeable", FLAG_RESIZEABLE_FOR_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:restoreAnyVersion", FLAG_RESTORE_ANY_VERSION);
        applicationFlags += getApplicationFlag(manifestDocument, "android:largeScreens", FLAG_SUPPORTS_LARGE_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:normalScreens", FLAG_SUPPORTS_NORMAL_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:anyDensity", FLAG_SUPPORTS_SCREEN_DENSITIES);
        applicationFlags += getApplicationFlag(manifestDocument, "android:smallScreens", FLAG_SUPPORTS_SMALL_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:testOnly", FLAG_TEST_ONLY);
        applicationFlags += getApplicationFlag(manifestDocument, "android:vmSafeMode", FLAG_VM_SAFE_MODE);
    }

    private int getApplicationFlag(Document doc, String attribute, int attributeValue) {
    	String flagString = getTagAttributeText(doc, "application", attribute);
    	return "true".equalsIgnoreCase(flagString) ? attributeValue : 0;
    }

    private int getTagAttributeIntValue(Document doc, String tag, String attribute, int defaultValue) {
        String valueString = getTagAttributeText(doc, tag, attribute);
        if (valueString != null) {
            return Integer.parseInt(valueString);
        }
        return defaultValue;
    }

    public String getApplicationName() {
        parseAndroidManifest();
        return applicationName;
    }

    public String getPackageName() {
        parseAndroidManifest();
        return packageName;
    }

    public int getMinSdkVersion() {
    	parseAndroidManifest();
		return minSdkVersion;
	}

    public int getSdkVersion() {
        parseAndroidManifest();
        return sdkVersion;
    }

    public int getApplicationFlags() {
    	parseAndroidManifest();
    	return applicationFlags;
    }

    public String getProcessName() {
		parseAndroidManifest();
		return processName;
	}

    public File getResourceDirectory() {
        return resourceDirectory;
    }

    public File getAssetsDirectory() {
        return assetsDirectory;
    }

    private static String getTagAttributeText(Document doc, String tag, String attribute) {
        NodeList elementsByTagName = doc.getElementsByTagName(tag);
        for (int i = 0; i < elementsByTagName.getLength(); ++i) {
            Node item = elementsByTagName.item(i);
            Node namedItem = item.getAttributes().getNamedItem(attribute);
            if (namedItem != null) {
                return namedItem.getTextContent();
            }
        }
        return null;
    }

    private static Application newApplicationInstance(String packageName, String applicationName) {
        Application application;
        try {
            Class<? extends Application> applicationClass =
                    new ClassNameResolver<Application>(packageName, applicationName).resolve();
            application = applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobolectricConfig that = (RobolectricConfig) o;

        if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile) : that.androidManifestFile != null)
            return false;
        if (getAssetsDirectory() != null ? !getAssetsDirectory().equals(that.getAssetsDirectory()) : that.getAssetsDirectory() != null)
            return false;
        if (getResourceDirectory() != null ? !getResourceDirectory().equals(that.getResourceDirectory()) : that.getResourceDirectory() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
        result = 31 * result + (getResourceDirectory() != null ? getResourceDirectory().hashCode() : 0);
        result = 31 * result + (getAssetsDirectory() != null ? getAssetsDirectory().hashCode() : 0);
        return result;
    }
}