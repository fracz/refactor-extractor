package org.jetbrains.idea.maven.embedder;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.maven.embedder.*;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.utils.JDOMReader;
import org.jetbrains.idea.maven.utils.MavenConstants;
import org.jetbrains.idea.maven.utils.MavenLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MavenEmbedderFactory {
  @NonNls private static final String PROP_MAVEN_HOME = "maven.home";
  @NonNls private static final String PROP_USER_HOME = "user.home";
  @NonNls private static final String ENV_M2_HOME = "M2_HOME";

  @NonNls private static final String M2_DIR = "m2";
  @NonNls private static final String BIN_DIR = "bin";
  @NonNls private static final String DOT_M2_DIR = ".m2";
  @NonNls private static final String CONF_DIR = "conf";
  @NonNls private static final String M2_CONF_FILE = "m2.conf";

  @NonNls private static final String REPOSITORY_DIR = "repository";

  @NonNls private static final String LOCAL_REPOSITORY_TAG = "localRepository";

  @NonNls private static final String[] standardPhases = {"clean", "compile", "test", "package", "install", "site"};
  @NonNls private static final String[] standardGoals = {"clean", "validate", "generate-sources", "process-sources", "generate-resources",
    "process-resources", "compile", "process-classes", "generate-test-sources", "process-test-sources", "generate-test-resources",
    "process-test-resources", "test-compile", "test", "package", "pre-integration-test", "integration-test", "post-integration-test",
    "verify", "install", "site", "deploy"};

  private static volatile Properties mySystemPropertiesCache;

  @Nullable
  public static File resolveMavenHomeDirectory(@Nullable String overrideMavenHome) {
    if (!StringUtil.isEmptyOrSpaces(overrideMavenHome)) {
      return new File(overrideMavenHome);
    }

    final String m2home = System.getenv(ENV_M2_HOME);
    if (!StringUtil.isEmptyOrSpaces(m2home)) {
      final File homeFromEnv = new File(m2home);
      if (isValidMavenHome(homeFromEnv)) {
        return homeFromEnv;
      }
    }

    final String userHome = System.getProperty(PROP_USER_HOME);
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      final File underUserHome = new File(userHome, M2_DIR);
      if (isValidMavenHome(underUserHome)) {
        return underUserHome;
      }
    }

    return null;
  }

  public static boolean isValidMavenHome(File home) {
    return getMavenConfFile(home).exists();
  }

  public static File getMavenConfFile(File mavenHome) {
    return new File(new File(mavenHome, BIN_DIR), M2_CONF_FILE);
  }

  @Nullable
  public static File resolveGlobalSettingsFile(@Nullable String overrideMavenHome) {
    final File directory = resolveMavenHomeDirectory(overrideMavenHome);
    if (directory != null) {
      final File file = new File(new File(directory, CONF_DIR), MavenConstants.SETTINGS_XML);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  @Nullable
  public static File resolveUserSettingsFile(@Nullable String overrideSettingsFile) {
    if (!StringUtil.isEmptyOrSpaces(overrideSettingsFile)) {
      return new File(overrideSettingsFile);
    }
    final String userHome = System.getProperty(PROP_USER_HOME);
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      final File file = new File(new File(userHome, DOT_M2_DIR), MavenConstants.SETTINGS_XML);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  @Nullable
  public static File resolveLocalRepository(@Nullable String mavenHome, @Nullable String userSettings, @Nullable String override) {
    if (!StringUtil.isEmpty(override)) {
      return new File(override);
    }

    final File userSettingsFile = resolveUserSettingsFile(userSettings);
    if (userSettingsFile != null) {
      final String fromUserSettings = getRepositoryFromSettings(userSettingsFile);
      if (!StringUtil.isEmpty(fromUserSettings)) {
        return new File(fromUserSettings);
      }
    }

    final File globalSettingsFile = resolveGlobalSettingsFile(mavenHome);
    if (globalSettingsFile != null) {
      final String fromGlobalSettings = getRepositoryFromSettings(globalSettingsFile);
      if (!StringUtil.isEmpty(fromGlobalSettings)) {
        return new File(fromGlobalSettings);
      }
    }

    return new File(new File(System.getProperty(PROP_USER_HOME), DOT_M2_DIR), REPOSITORY_DIR);
  }

  private static String getRepositoryFromSettings(File file) {
    try {
      FileInputStream is = new FileInputStream(file);
      try {
        JDOMReader reader = new JDOMReader(is);
        return reader.getChildText(reader.getRootElement(), LOCAL_REPOSITORY_TAG);
      }
      finally {
        is.close();
      }
    }
    catch (IOException ignore) {
      return null;
    }
  }

  public static List<String> getStandardPhasesList() {
    return Arrays.asList(standardPhases);
  }

  public static List<String> getStandardGoalsList() {
    return Arrays.asList(standardGoals);
  }

  public static MavenEmbedderWrapper createEmbedder(MavenGeneralSettings settings) {
    Configuration configuration = new DefaultConfiguration();

    configuration.setConfigurationCustomizer(MavenEmbedderWrapper.createCustomizer());
    configuration.setClassLoader(settings.getClass().getClassLoader());
    configuration.setLocalRepository(settings.getEffectiveLocalRepository());

    MavenEmbedderLogger logger = new MavenConsoleLogger();
    logger.setThreshold(settings.getOutputLevel());
    configuration.setMavenEmbedderLogger(logger);

    File userSettingsFile = resolveUserSettingsFile(settings.getMavenSettingsFile());
    if (userSettingsFile != null) {
      configuration.setUserSettingsFile(userSettingsFile);
    }

    File globalSettingsFile = resolveGlobalSettingsFile(settings.getMavenHome());
    if (globalSettingsFile != null) {
      configuration.setGlobalSettingsFile(globalSettingsFile);
    }

    configuration.setSystemProperties(collectSystemProperties());

    validate(configuration);

    System.setProperty(PROP_MAVEN_HOME, settings.getMavenHome());

    try {
      MavenEmbedder e = new MavenEmbedder(configuration);
      e.getSettings().setUsePluginRegistry(settings.isUsePluginRegistry());
      return new MavenEmbedderWrapper(e);
    }
    catch (MavenEmbedderException e) {
      MavenLog.LOG.info(e);
      throw new RuntimeException(e);
    }
  }

  public static Properties collectSystemProperties() {
    if (mySystemPropertiesCache == null) {
      Properties result = new Properties();
      result.putAll(System.getProperties());

      try {
        Properties envVars = CommandLineUtils.getSystemEnvVars();
        for (Map.Entry<Object, Object> each : envVars.entrySet()) {
          result.setProperty("env." + each.getKey().toString(), each.getValue().toString());
        }
      }
      catch (IOException e) {
        MavenLog.LOG.warn(e);
      }

      mySystemPropertiesCache = result;
    }

    return mySystemPropertiesCache;
  }

  private static void validate(Configuration configuration) {
    ConfigurationValidationResult result = MavenEmbedder.validateConfiguration(configuration);

    if (!result.isValid()) {
      if (result.getGlobalSettingsException() != null) {
        configuration.setGlobalSettingsFile(null);
      }
      if (result.getUserSettingsException() != null) {
        configuration.setUserSettingsFile(null);
      }
    }
  }
}