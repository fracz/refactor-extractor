/*
 * Copyright 2000-2008 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.config;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.roots.ui.configuration.LibraryTableModifiableModelProvider;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigrableContext;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.config.GrailsConfigUtils;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.settings.GroovyApplicationSettings;
import org.jetbrains.plugins.groovy.util.GroovyUtils;
import org.jetbrains.plugins.groovy.util.LibrariesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author ilyas
 */
public class GroovyConfigUtils {
  private static final String GROOVY_STARTER_FILE_NAME = "groovy";
  public static final String UNDEFINED_VERSION = "undefined";
  public static final String GROOVY_LIB_PATTERN = "groovy-\\d.*";
  public static final String GROOVY_JAR_PATTERN = "groovy-all-\\d.*jar";
  public static final String GROOVY_LIB_PREFIX = "groovy-";

  public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
  public static final String DGM_CLASS_PATH = "org/codehaus/groovy/runtime/DefaultGroovyMethods.class";
  public static final String CLOSURE_CLASS_PATH = "org/codehaus/groovy/runtime/DefaultGroovyMethods.class";

  public static boolean isGroovySdkHome(final VirtualFile file) {
    final Ref<Boolean> result = Ref.create(false);
    processFilesUnderGDKRoot(file, new Processor<VirtualFile>() {
      public boolean process(final VirtualFile virtualFile) {
        result.set(true);
        return false;
      }
    });
    return result.get();
  }

  private static void processFilesUnderGDKRoot(VirtualFile file, final Processor<VirtualFile> processor) {
    if (file != null && file.isDirectory()) {
      final VirtualFile child = file.findChild("bin");

      if (child != null && child.isDirectory()) {
        for (VirtualFile grandChild : child.getChildren()) {
          if (GROOVY_STARTER_FILE_NAME.equals(grandChild.getNameWithoutExtension())) {
            if (!processor.process(grandChild)) return;
          }
        }
      }
    }
  }

  public static String getGroovyVersion(@NotNull String path) {
    String groovyJarVersion = getGroovyGrailsJarVersion(path + "/lib", "groovy-\\d.*\\.jar", MANIFEST_PATH);
    return groovyJarVersion != null ? groovyJarVersion : UNDEFINED_VERSION;
  }

  /**
   * Return value of Implementation-Version attribute in jar manifest
   * <p/>
   *
   * @param jarPath      directory containing jar file
   * @param jarRegex     filename pattern for jar file
   * @param manifestPath path to manifest file in jar file
   * @return value of Implementation-Version attribute, null if not found
   */
  public static String getGroovyGrailsJarVersion(String jarPath, final String jarRegex, String manifestPath) {
    try {
      File[] jars = GroovyUtils.getFilesInDirectoryByPattern(jarPath, jarRegex);
      if (jars.length != 1) {
        return null;
      }
      JarFile jarFile = new JarFile(jars[0]);
      JarEntry jarEntry = jarFile.getJarEntry(manifestPath);
      if (jarEntry == null) {
        return null;
      }
      Manifest manifest = new Manifest(jarFile.getInputStream(jarEntry));
      return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    }
    catch (Exception e) {
      return null;
    }
  }

  public static Library[] getGroovyLibraries() {
    Condition<Library> condition = new Condition<Library>() {
      public boolean value(Library library) {
        return isGroovySdkLibrary(library);
      }
    };
    return LibrariesUtil.getLibraries(condition);
  }

  public static String[] getGroovyLibNames() {
    return LibrariesUtil.getLibNames(getGroovyLibraries());
  }

  public static boolean isGroovySdkLibrary(Library library) {
    VirtualFile[] classFiles = library.getFiles(OrderRootType.CLASSES);
    for (VirtualFile file : classFiles) {
      String path = file.getPath();
      if (path != null && "jar".equals(file.getExtension())) {
        path = StringUtil.trimEnd(path, "!/");
        String name = file.getName();
        if ((name.matches(GROOVY_JAR_PATTERN) || name.matches(GROOVY_LIB_PATTERN))) {
          File realFile = new File(path);
          if (realFile.exists()) {
            try {
              JarFile jarFile = new JarFile(realFile);
              return isGroovyJar(jarFile) && !GrailsConfigUtils.isGrailsSdkLibrary(library);
            } catch (IOException e) {
              return false;
            }
          }
        }
      }
    }
    return false;
  }

  public static boolean isGroovyJar(JarFile jarFile) {
    return jarFile.getJarEntry(MANIFEST_PATH) != null &&
        jarFile.getJarEntry(CLOSURE_CLASS_PATH) != null &&
        jarFile.getJarEntry(DGM_CLASS_PATH) != null;
  }

  @Nullable
  public static String getGroovyLibVersion(Library library) {
    return getGroovyVersion(LibrariesUtil.getGroovyOrGrailsLibraryHome(library));
  }

  public static GroovySDK[] getGroovySDKs() {
    return ContainerUtil.map2Array(getGroovyLibraries(), GroovySDK.class, new Function<Library, GroovySDK>() {
      public GroovySDK fun(final Library library) {
        return new GroovySDK(library);
      }
    });
  }

  public static void updateGroovyLibInModule(@NotNull Module module, @Nullable GroovySDK sdk) {
    ModuleRootManager manager = ModuleRootManager.getInstance(module);
    ModifiableRootModel model = manager.getModifiableModel();
    removeGroovyLibrariesFormModule(model);
    if (sdk == null || sdk.getLibrary() == null) {
      model.commit();
      return;
    }

    saveGroovyDefaultLibName(sdk.getLibraryName());
    Library newLib = sdk.getLibrary();
    LibraryOrderEntry addedEntry = model.addLibraryEntry(newLib);
    LibrariesUtil.placeEntryToCorrectPlace(model, addedEntry);
    model.commit();
  }

  public static void removeGroovyLibrariesFormModule(ModifiableRootModel model) {
    OrderEntry[] entries = model.getOrderEntries();
    for (OrderEntry entry : entries) {
      if (entry instanceof LibraryOrderEntry) {
        LibraryOrderEntry libEntry = (LibraryOrderEntry) entry;
        Library library = libEntry.getLibrary();
        if (isGroovySdkLibrary(library)) {
          model.removeOrderEntry(entry);
        }
      }
    }
  }

  public static Library[] getGroovyLibrariesByModule(final Module module) {
    final Condition<Library> condition = new Condition<Library>() {
      public boolean value(Library library) {
        return isGroovySdkLibrary(library);
      }
    };
    return LibrariesUtil.getLibrariesByCondition(module, condition);
  }

  public static ValidationResult isGroovySdkHome(String path) {
    if (path != null) {
      final VirtualFile relativeFile = VfsUtil.findRelativeFile(path, null);
      if (relativeFile != null && GroovyConfigUtils.isGroovySdkHome(relativeFile)) {
        return ValidationResult.OK;
      }
    }
    return new ValidationResult(GroovyBundle.message("invalid.groovy.sdk.path.message"));
  }

  public static Library createGroovyLibrary(final String path, final String name, final @Nullable Project project, final boolean inModuleSettings) {
    if (project == null) return null;
    final Ref<Library> libRef = new Ref<Library>();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        Library library = createGroovyLibImmediately(path, name, project, inModuleSettings);
        libRef.set(library);
      }
    });
    return libRef.get();
  }

  private static Library createGroovyLibImmediately(String path, String name, Project project, boolean inModuleSettings) {
    String version = getGroovyVersion(path);
    String libName = name != null ? name : generateNewGroovyLibName(version);
    if (path.length() > 0) {
      // create library
      LibraryTable.ModifiableModel modifiableModel = null;
      Library library;

      if (inModuleSettings) {
        StructureConfigrableContext context = ModuleStructureConfigurable.getInstance(project).getContext();
        LibraryTableModifiableModelProvider provider = context.createModifiableModelProvider(LibraryTablesRegistrar.APPLICATION_LEVEL, true);
        modifiableModel = provider.getModifiableModel();
        library = modifiableModel.createLibrary(libName);
      } else {
        LibraryTable libTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
        library = libTable.getLibraryByName(libName);
        if (library == null) {
          library = LibraryUtil.createLibrary(libTable, libName);
        }
      }

      // fill library
      final Library.ModifiableModel model;
      if (inModuleSettings) {
        model = ((LibrariesModifiableModel) modifiableModel).getLibraryEditor(library).getModel();
      } else {
        model = library.getModifiableModel();
      }
      File srcRoot = new File(path + "/src/main");
      if (srcRoot.exists()) {
        model.addRoot(VfsUtil.getUrlForLibraryRoot(srcRoot), OrderRootType.SOURCES);
      }

      File[] jars;
      File embeddableDir = new File(path + "/embeddable");
      if (embeddableDir.exists()) {
        jars = embeddableDir.listFiles();
      } else {
        jars = new File(path + "/lib").listFiles();
      }
      if (jars != null) {
        for (File file : jars) {
          if (file.getName().endsWith(".jar")) {
            model.addRoot(VfsUtil.getUrlForLibraryRoot(file), OrderRootType.CLASSES);
          }
        }
      }
      if (!inModuleSettings) {
        model.commit();
      }
      return library;
    }
    return null;
  }

  public static String generateNewGroovyLibName(String version) {
    String prefix = GROOVY_LIB_PREFIX;
    return LibrariesUtil.generateNewLibraryName(version, prefix);
  }

  public static void saveGroovyDefaultLibName(String name) {
    GroovyApplicationSettings settings = GroovyApplicationSettings.getInstance();
    if (!UNDEFINED_VERSION.equals(name)) {
      settings.DEFAULT_GROOVY_LIB_NAME = name;
    }
  }

  @Nullable
  public static String getGroovyDefaultLibName() {
    GroovyApplicationSettings settings = GroovyApplicationSettings.getInstance();
    return settings.DEFAULT_GROOVY_LIB_NAME;
  }

  public static Library createLibFirstTime(String baseName) {
    LibraryTable libTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
    Library library = libTable.getLibraryByName(baseName);
    if (library == null) {
      library = LibraryUtil.createLibrary(libTable, baseName);
    }
    return library;
  }

  public static void removeOldRoots(Library.ModifiableModel model) {
    for (OrderRootType type : OrderRootType.ALL_TYPES)
      for (String url : model.getUrls(type))
        model.removeRoot(url, type);
  }

  public static Collection<String> getGroovyVersions() {
    return ContainerUtil.map2List(getGroovyLibraries(), new Function<Library, String>() {
      public String fun(Library library) {
        return getGroovyLibVersion(library);
      }
    });
  }

  public static boolean isGroovyConfigured(Module module) {
    return module != null && getGroovyLibrariesByModule(module).length > 0 ||
        GrailsConfigUtils.isGrailsConfigured(module);
  }

  @NotNull
  public static String getGroovyInstallPath(Module module) {
    if (module == null) return "";
    Library[] libraries = getGroovyLibrariesByModule(module);
    if (libraries.length == 0) return "";
    Library library = libraries[0];
    return LibrariesUtil.getGroovyOrGrailsLibraryHome(library);
  }

}