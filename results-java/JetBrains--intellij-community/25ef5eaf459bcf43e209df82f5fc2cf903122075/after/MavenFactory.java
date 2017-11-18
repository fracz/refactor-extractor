package org.jetbrains.idea.maven.core.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.embedder.*;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.core.MavenCoreSettings;
import org.jetbrains.idea.maven.project.MavenException;
import org.jetbrains.idea.maven.project.MavenToIdeaMapping;
import org.jetbrains.idea.maven.project.ProjectArtifactResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vladislav.Kaznacheev
 */
public class MavenFactory {
  public static MavenToIdeaMapping ourMapping;

  private static final Logger LOG = Logger.getInstance("#" + MavenFactory.class.getName());

  @NonNls private static final String PROP_MAVEN_HOME = "maven.home";
  @NonNls private static final String PROP_USER_HOME = "user.home";
  @NonNls private static final String ENV_M2_HOME = "M2_HOME";

  @NonNls private static final String M2_DIR = "m2";
  @NonNls private static final String BIN_DIR = "bin";
  @NonNls private static final String DOT_M2_DIR = ".m2";
  @NonNls private static final String CONF_DIR = "conf";
  @NonNls private static final String SETTINGS_FILE = "settings.xml";
  @NonNls private static final String M2_CONF_FILE = "m2.conf";

  @NonNls private static final String REPOSITORY_DIR = "repository";

  @NonNls private static final String LOCAL_REPOSITORY_TAG = "localRepository";

  @NonNls private static final String[] standardPhases = {"clean", "compile", "test", "package", "install", "site"};
  @NonNls private static final String[] standardGoals = {"clean", "validate", "generate-sources", "process-sources", "generate-resources",
    "process-resources", "compile", "process-classes", "generate-test-sources", "process-test-sources", "generate-test-resources",
    "process-test-resources", "test-compile", "test", "package", "pre-integration-test", "integration-test", "post-integration-test",
    "verify", "install", "site", "deploy"};

  @Nullable
  public static File resolveMavenHomeDirectory(@Nullable final String override) {
    if (!StringUtil.isEmptyOrSpaces(override)) {
      return new File(override);
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
  public static File resolveGlobalSettingsFile(final String override) {
    final File directory = resolveMavenHomeDirectory(override);
    if (directory != null) {
      final File file = new File(new File(directory, CONF_DIR), SETTINGS_FILE);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  @Nullable
  public static File resolveUserSettingsFile(final String override) {
    if (!StringUtil.isEmptyOrSpaces(override)) {
      return new File(override);
    }
    final String userHome = System.getProperty(PROP_USER_HOME);
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      final File file = new File(new File(userHome, DOT_M2_DIR), SETTINGS_FILE);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  @Nullable
  public static File resolveLocalRepository(final String mavenHome, final String userSettings, final String override) {
    if (!StringUtil.isEmpty(override)) {
      return new File(override);
    }

    final File userSettingsFile = resolveUserSettingsFile(userSettings);
    if (userSettingsFile != null) {
      final String fromUserSettings = MavenFactory.getRepositoryFromSettings(userSettingsFile);
      if (!StringUtil.isEmpty(fromUserSettings)) {
        return new File(fromUserSettings);
      }
    }

    final File globalSettingsFile = resolveGlobalSettingsFile(mavenHome);
    if (globalSettingsFile != null) {
      final String fromGlobalSettings = MavenFactory.getRepositoryFromSettings(globalSettingsFile);
      if (!StringUtil.isEmpty(fromGlobalSettings)) {
        return new File(fromGlobalSettings);
      }
    }

    final String userHome = System.getProperty(PROP_USER_HOME);
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      return new File(new File(userHome, DOT_M2_DIR), REPOSITORY_DIR);
    }

    return null;
  }

  public static String getRepositoryFromSettings(File file) {
    JDOMReader reader = new JDOMReader();
    try {
      reader.init(new FileInputStream(file));
    }
    catch (IOException ignore) {
    }
    return reader.getChildText(reader.getRootElement(), LOCAL_REPOSITORY_TAG);
  }

  public static List<String> getStandardPhasesList() {
    return Arrays.asList(standardPhases);
  }

  public static List<String> getStandardGoalsList() {
    return Arrays.asList(standardGoals);
  }

  public static MavenEmbedder createEmbedder(MavenCoreSettings settings) throws MavenException {
    return createEmbedder(settings, null);
  }

  public static MavenEmbedder createEmbedder(MavenCoreSettings settings, ContainerCustomizer customizer) throws MavenException {
    return createEmbedder(settings.getMavenHome(),
                          settings.getEffectiveLocalRepository(),
                          settings.getMavenSettingsFile(),
                          settings.getClass().getClassLoader(),
                          customizer);
  }

  private static MavenEmbedder createEmbedder(String mavenHome,
                                              File localRepo,
                                              String userSettings,
                                              ClassLoader classLoader,
                                              ContainerCustomizer customizer) throws MavenException {
    Configuration configuration = new DefaultConfiguration();
    configuration.setConfigurationCustomizer(customizer);

    configuration.setLocalRepository(localRepo);

    MavenEmbedderConsoleLogger l = new MavenEmbedderConsoleLogger();
    l.setThreshold(MavenEmbedderLogger.LEVEL_WARN);
    configuration.setMavenEmbedderLogger(l);

    final File userSettingsFile = resolveUserSettingsFile(userSettings);
    if (userSettingsFile != null) {
      configuration.setUserSettingsFile(userSettingsFile);
    }

    final File globalSettingsFile = resolveGlobalSettingsFile(mavenHome);
    if (globalSettingsFile != null) {
      configuration.setGlobalSettingsFile(globalSettingsFile);
    }

    configuration.setClassLoader(classLoader);

    ConfigurationValidationResult result = MavenEmbedder.validateConfiguration(configuration);

    if (!result.isValid()) {
      throw new MavenException(collectExceptions(result));
    }

    System.setProperty(PROP_MAVEN_HOME, mavenHome);

    try {
      return new MavenEmbedder(configuration);
    }
    catch (MavenEmbedderException e) {
      LOG.info(e);
      throw new MavenException(e);
    }
  }

  public static ContainerCustomizer createCustomizerWithMapping(final MavenToIdeaMapping mapping) {
    return new ContainerCustomizer() {
      public void customize(PlexusContainer c) {
        // TODO this is an ugly hack in order to to pass the parameter to the component.
        // I have to find a way to do it correctly.
        ourMapping = mapping;
        ComponentDescriptor resolverDescriptor = c.getComponentDescriptor(ArtifactResolver.ROLE);
        resolverDescriptor.setImplementation(ProjectArtifactResolver.class.getName());
      }
    };
  }

  private static List<Exception> collectExceptions(ConfigurationValidationResult result) {
    List<Exception> ee = new ArrayList<Exception>();

    Exception ex1 = result.getGlobalSettingsException();
    Exception ex2 = result.getUserSettingsException();
    if (ex1 != null) ee.add(ex1);
    if (ex2 != null) ee.add(ex2);

    return ee;
  }

  public static void releaseEmbedder(MavenEmbedder mavenEmbedder) {
    if (mavenEmbedder != null) {
      try {
        mavenEmbedder.stop();
      }
      catch (MavenEmbedderException ignore) {
      }
    }
  }
}