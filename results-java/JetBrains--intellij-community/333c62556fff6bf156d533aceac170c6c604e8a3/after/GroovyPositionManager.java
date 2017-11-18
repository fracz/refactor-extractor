/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.debugger;

import com.intellij.debugger.NoDataException;
import com.intellij.debugger.PositionManager;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.CompoundPositionManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.jdi.VirtualMachineProxy;
import com.intellij.debugger.requests.ClassPrepareRequestor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.HashSet;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ClassPrepareRequest;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gant.GantFileType;
import org.jetbrains.plugins.gant.GantUtils;
import org.jetbrains.plugins.groovy.GroovyLoader;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GroovyPositionManager implements PositionManager {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.engine.PositionManagerImpl");

  private final DebugProcess myDebugProcess;
  @NonNls private static final String GANT_SUFFIX = "_gant";

  public GroovyPositionManager(DebugProcess debugProcess) {
    myDebugProcess = debugProcess;
  }

  public DebugProcess getDebugProcess() {
    return myDebugProcess;
  }

  @NotNull
  public List<Location> locationsOfLine(ReferenceType type, SourcePosition position) throws NoDataException {
    try {
      int line = position.getLine() + 1;
      List<Location> locations = getDebugProcess().getVirtualMachineProxy().versionHigher("1.4")
                                 ? type.locationsOfLine(DebugProcessImpl.JAVA_STRATUM, null, line)
                                 : type.locationsOfLine(line);
      if (locations == null || locations.isEmpty()) throw new NoDataException();
      return locations;
    }
    catch (AbsentInformationException e) {
      throw new NoDataException();
    }
  }

  private static GroovyPsiElement findReferenceTypeSourceImage(SourcePosition position) {
    PsiFile file = position.getFile();
    if (!(file instanceof GroovyFileBase)) return null;
    PsiElement element = file.findElementAt(position.getOffset());
    if (element == null) return null;
    return PsiTreeUtil.getParentOfType(element, GrClosableBlock.class, GrTypeDefinition.class);
  }

  private GrTypeDefinition findEnclosingTypeDefinition(SourcePosition position) {
    PsiFile file = position.getFile();
    if (!(file instanceof GroovyFileBase)) return null;
    PsiElement element = file.findElementAt(position.getOffset());
    if (element == null) return null;
    return PsiTreeUtil.getParentOfType(element, GrTypeDefinition.class);
  }

  public ClassPrepareRequest createPrepareRequest(final ClassPrepareRequestor requestor, final SourcePosition position)
    throws NoDataException {
    GroovyPsiElement sourceImage = findReferenceTypeSourceImage(position);
    String qName = null;
    if (sourceImage instanceof GrTypeDefinition) {
      qName = ((GrTypeDefinition)sourceImage).getQualifiedName();
    } else if (sourceImage == null) {
      qName = getScriptQualifiedName(position);
    }

    String waitPrepareFor;
    ClassPrepareRequestor waitRequestor;

    if (qName == null) {
      GrTypeDefinition typeDefinition = findEnclosingTypeDefinition(position);

      if (typeDefinition != null) {
        qName = typeDefinition.getQualifiedName();
      } else {
        qName = getScriptQualifiedName(position);
      }

      if (qName == null) throw new NoDataException();
      waitPrepareFor = qName + "$*";
      waitRequestor = new ClassPrepareRequestor() {
        public void processClassPrepare(DebugProcess debuggerProcess, ReferenceType referenceType) {
          final CompoundPositionManager positionManager = ((DebugProcessImpl)debuggerProcess).getPositionManager();
          if (positionManager.locationsOfLine(referenceType, position).size() > 0) {
            requestor.processClassPrepare(debuggerProcess, referenceType);
          } else {
            final List<ReferenceType> positionClasses = positionManager.getAllClasses(position);
            if (positionClasses.contains(referenceType)) {
              requestor.processClassPrepare(debuggerProcess, referenceType);
            }
          }
        }
      };
    } else {
      waitPrepareFor = qName;
      waitRequestor = requestor;
    }

    return myDebugProcess.getRequestsManager().createClassPrepareRequest(waitRequestor, waitPrepareFor);
  }

  @Nullable
  private static String getScriptQualifiedName(SourcePosition position) {
    PsiFile file = position.getFile();
    if (file instanceof GroovyFile) {
      return getScriptFQName((GroovyFile)file);
    }
    return null;
  }

  public SourcePosition getSourcePosition(final Location location) throws NoDataException {
    if (location == null) throw new NoDataException();

    PsiFile psiFile = getPsiFileByLocation(getDebugProcess().getProject(), location);
    if (psiFile == null) throw new NoDataException();

    int lineNumber = calcLineIndex(location);
    if (lineNumber < 0) throw new NoDataException();
    return SourcePosition.createFromLine(psiFile, lineNumber);
  }

  private int calcLineIndex(Location location) {
    LOG.assertTrue(myDebugProcess != null);
    if (location == null) return -1;

    try {
      return location.lineNumber() - 1;
    }
    catch (InternalError e) {
      return -1;
    }
  }

  @Nullable
  private PsiFile getPsiFileByLocation(final Project project, final Location location) {
    if (location == null) return null;

    final ReferenceType refType = location.declaringType();
    if (refType == null) return null;

    final String originalQName = refType.name().replace('/', '.');
    int dollar = originalQName.indexOf('$');
    String qName = dollar >= 0 ? originalQName.substring(0, dollar) : originalQName;

    // For Gant scripts
    String originalName = qName;
    qName = processGantScriptFileName(originalName);

    final GlobalSearchScope searchScope = myDebugProcess.getSearchScope();
    final PsiClass[] classes = GroovyPsiManager.getInstance(project).getNamesCache().getClassesByFQName(qName, searchScope);
    PsiClass clazz = classes.length == 1 ? classes[0] : null;
    if (clazz != null) return clazz.getContainingFile();

    DirectoryIndex directoryIndex = DirectoryIndex.getInstance(project);
    int dotIndex = qName.lastIndexOf(".");
    String packageName = dotIndex > 0 ? qName.substring(0, dotIndex) : "";
    Query<VirtualFile> query = directoryIndex.getDirectoriesByPackageName(packageName, true);
    String fileNameWithoutExtension = dotIndex > 0 ? qName.substring(dotIndex + 1) : qName;
    final Set<String> fileNames = new HashSet<String>();
    for (final String extention : GroovyLoader.GROOVY_EXTENSIONS) {
      fileNames.add(fileNameWithoutExtension + "." + extention);
    }
    final Ref<PsiFile> result = new Ref<PsiFile>();
    query.forEach(new Processor<VirtualFile>() {
      public boolean process(VirtualFile vDir) {
        for (final String fileName : fileNames) {
          VirtualFile vFile = vDir.findChild(fileName);
          if (vFile != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
            if (psiFile instanceof GroovyFileBase) {
              result.set(psiFile);
              return false;
            }
          }
        }
        return true;
      }
    });

    PsiFile res = result.get();
    if (res != null) {
      return res;
    }
    // For gant scripts
    if (GantUtils.isGantScriptCompiledName(originalName)) {
      PsiFile[] files =
        FilenameIndex.getFilesByName(project, qName + "." + GantFileType.DEFAULT_EXTENSION, GlobalSearchScope.allScope(project));
      if (files.length == 1) return files[0];
    }
    return null;
  }

  private static String processGantScriptFileName(String qName) {
    if (qName.endsWith(GANT_SUFFIX)) {
      qName = StringUtil.trimEnd(qName, GANT_SUFFIX);
    }
    return qName;
  }

  @NotNull
  public List<ReferenceType> getAllClasses(final SourcePosition position) throws NoDataException {
    List<ReferenceType> result = ApplicationManager.getApplication().runReadAction(new Computable<List<ReferenceType>>() {
      public List<ReferenceType> compute() {
        GroovyPsiElement sourceImage = findReferenceTypeSourceImage(position);
        final String scriptName = getScriptQualifiedName(position);

        if (sourceImage instanceof GrTypeDefinition) {
          String qName = ((GrTypeDefinition)sourceImage).getQualifiedName();
          if (qName != null) return myDebugProcess.getVirtualMachineProxy().classesByName(qName);
        } else if (sourceImage == null) {
          if (scriptName != null) return myDebugProcess.getVirtualMachineProxy().classesByName(scriptName);
        } else {
          final GrTypeDefinition typeDefinition = findEnclosingTypeDefinition(position);
          String enclosingName;
          if (typeDefinition != null) {
            enclosingName = typeDefinition.getQualifiedName();
          } else {
            enclosingName = scriptName;
          }
          if (enclosingName == null) return Collections.emptyList();

          final List<ReferenceType> outers = myDebugProcess.getVirtualMachineProxy().classesByName(enclosingName);
          final List<ReferenceType> result = new ArrayList<ReferenceType>(outers.size());
          for (ReferenceType outer : outers) {
            final ReferenceType nested = findNested(outer, sourceImage, position);
            if (nested != null) {
              result.add(nested);
            }
          }
          return result;
        }
        return Collections.emptyList();
      }
    });

    if (result == null || result.isEmpty()) throw new NoDataException();
    return result;
  }

  private static String getScriptFQName(GroovyFile groovyFile) {
    String qName;
    VirtualFile vFile = groovyFile.getVirtualFile();
    assert vFile != null;
    String packageName = groovyFile.getPackageName();
    String plainName = vFile.getNameWithoutExtension();
    String fileName = GantUtils.isGantScriptFile(groovyFile) ? plainName + GANT_SUFFIX : plainName;
    qName = packageName.length() > 0 ? packageName + "." + fileName : fileName;
    return qName;
  }

  @Nullable
  private ReferenceType findNested(ReferenceType fromClass, final GroovyPsiElement toFind, SourcePosition classPosition) {
    final VirtualMachineProxy vmProxy = myDebugProcess.getVirtualMachineProxy();
    if (fromClass.isPrepared()) {

      final List<ReferenceType> nestedTypes = vmProxy.nestedTypes(fromClass);

      for (ReferenceType nested : nestedTypes) {
        final ReferenceType found = findNested(nested, toFind, classPosition);
        if (found != null) {
          return found;
        }
      }

      try {
        final int lineNumber = classPosition.getLine() + 1;
        if (fromClass.locationsOfLine(lineNumber).size() > 0) {
          return fromClass;
        }
        //noinspection LoopStatementThatDoesntLoop
        for (Location location : fromClass.allLineLocations()) {
          final SourcePosition candidateFirstPosition = SourcePosition.createFromLine(toFind.getContainingFile(), location.lineNumber() - 1)
            ;
          if (toFind.equals(findReferenceTypeSourceImage(candidateFirstPosition))) {
            return fromClass;
          }
          break; // check only the first location
        }
      }
      catch (AbsentInformationException ignored) {
      }
    }
    return null;
  }

}