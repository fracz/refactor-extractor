package org.jetbrains.plugins.groovy.compiler.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrConstructorInvocation;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrInterfaceDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrConstructor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMembersDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.packaging.GrPackageDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.util.containers.CharTrie;

import java.io.*;
import java.util.*;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 03.05.2007
 */
public class GroovyToJavaGenerator implements SourceGeneratingCompiler, CompilationStatusListener {
  private static final Map<String, String> typesToInitialValues = new HashMap<String, String>();
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.compiler.generator.GroovyToJavaGenerator");

  static {
    typesToInitialValues.put("boolean", "false");
    typesToInitialValues.put("int", "0");
    typesToInitialValues.put("short", "0");
    typesToInitialValues.put("long", "0L");
    typesToInitialValues.put("byte", "0");
    typesToInitialValues.put("char", "'c'");
    typesToInitialValues.put("double", "0D");
    typesToInitialValues.put("float", "0F");
    typesToInitialValues.put("void", "");
  }

  private static final String[] JAVA_MODIFIERS = new String[]{
      PsiModifier.PUBLIC,
      PsiModifier.PROTECTED,
      PsiModifier.PRIVATE,
      PsiModifier.PACKAGE_LOCAL,
      PsiModifier.STATIC,
      PsiModifier.ABSTRACT,
      PsiModifier.FINAL,
      PsiModifier.NATIVE,
      PsiModifier.SYNCHRONIZED,
      PsiModifier.STRICTFP,
      PsiModifier.TRANSIENT,
      PsiModifier.VOLATILE
  };

  private static final String[] JAVA_TYPE_DEFINITION_MODIFIERS = new String[]{
      PsiModifier.PUBLIC,
      PsiModifier.ABSTRACT,
      PsiModifier.FINAL
  };

  private static final CharSequence PREFIX_SEPARATOR = "/";
  private CompileContext myContext;
  private Project myProject;

  public GroovyToJavaGenerator(Project project) {
    myProject = project;
  }

  public GenerationItem[] getGenerationItems(CompileContext context) {
    myContext = context;

    List<GenerationItem> generationItems = new ArrayList<GenerationItem>();
    GenerationItem item;
    for (VirtualFile file : getGroovyFilesToGenerate(context)) {
      final GroovyFileBase psiFile = findPsiFile(file);
      boolean isInTestSources = ModuleRootManager.getInstance(getModuleByFile(context, file)).getFileIndex().isInTestSourceContent(file);

      GrTopStatement[] statements = getTopStatementsInReadAction(psiFile);

      boolean needCreateTopLevelClass = !needsCreateClassFromFileName(statements);

      String prefix = "";
      if (statements.length > 0 && statements[0] instanceof GrPackageDefinition) {
        prefix = getJavaClassPackage((GrPackageDefinition) statements[0]);
      }

      final Module module = getModuleByFile(context, file);

      //top level class
      if (needCreateTopLevelClass) {
        generationItems.add(new GenerationItemImpl(prefix + file.getNameWithoutExtension() + "." + "java", module, new TimestampValidityState(file.getTimeStamp()), isInTestSources, file));
      }

      GrTypeDefinition[] typeDefinitions = ApplicationManager.getApplication().runReadAction(new Computable<GrTypeDefinition[]>() {
        public GrTypeDefinition[] compute() {
          return psiFile.getTypeDefinitions();
        }
      });

      for (GrTypeDefinition typeDefinition : typeDefinitions) {
        item = new GenerationItemImpl(prefix + typeDefinition.getName() + "." + "java", module, new TimestampValidityState(file.getTimeStamp()), isInTestSources, file);
        generationItems.add(item);
      }
    }
    return generationItems.toArray(new GenerationItem[generationItems.size()]);
  }

  protected Module getModuleByFile(CompileContext context, VirtualFile file) {
    return context.getModuleByFile(file);
  }

  protected VirtualFile[] getGroovyFilesToGenerate(CompileContext context) {
    return context.getProjectCompileScope().getFiles(GroovyFileType.GROOVY_FILE_TYPE, true);
  }

  public GenerationItem[] generate(CompileContext context, GenerationItem[] itemsToGenerate, VirtualFile outputRootDirectory) {
    List<GenerationItem> generatedItems = new ArrayList<GenerationItem>();
    Map<String, GenerationItem> pathsToItemsMap = new HashMap<String, GenerationItem>();

    //puts items witch can be generated
    for (GenerationItem item : itemsToGenerate) {
      pathsToItemsMap.put(item.getPath(), item);
    }

    Set<VirtualFile> vFiles = new HashSet<VirtualFile>();
    for (GenerationItem item : itemsToGenerate) {
      vFiles.add(((GenerationItemImpl) item).getVFile());
    }

    for (VirtualFile vFile : vFiles) {
      //generate java classes form groovy source files
      List<String> generatedJavaFilesRelPaths = generateItems(vFile, outputRootDirectory);
      for (String relPath : generatedJavaFilesRelPaths) {
        GenerationItem generationItem = pathsToItemsMap.get(relPath);
        if (generationItem != null)
          generatedItems.add(generationItem);
      }
    }

    return generatedItems.toArray(new GenerationItem[generatedItems.size()]);
  }

  private GroovyFile findPsiFile(final VirtualFile virtualFile) {
    final GroovyFile[] myFindPsiFile = new GroovyFile[1];

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        myFindPsiFile[0] = (GroovyFile) PsiManager.getInstance(myProject).findFile(virtualFile);
      }
    });

    assert myFindPsiFile[0] != null;
    return myFindPsiFile[0];
  }

  //virtualFile -> PsiFile
  private List<String> generateItems(final VirtualFile item, final VirtualFile outputRootDirectory) {
    ProgressIndicator indicator = getProcessIndicator();
    if (indicator != null) indicator.setText(item.getPath());

    final GroovyFile file = findPsiFile(item);

    List<String> generatedJavaFilesRelPaths = ApplicationManager.getApplication().runReadAction(new Computable<List<String>>() {
      public List<String> compute() {
        return generate(file, outputRootDirectory);
      }
    });

    assert generatedJavaFilesRelPaths != null;

    return generatedJavaFilesRelPaths;
  }

  protected ProgressIndicator getProcessIndicator() {
    return myContext.getProgressIndicator();
  }

  private List<String> generate(final GroovyFile file, VirtualFile outputRootDirectory) {
    List<String> generatedItemsRelativePaths = new ArrayList<String>();

    GrTopStatement[] statements = getTopStatementsInReadAction(file);

    GrPackageDefinition packageDefinition = null;
    if (statements.length > 0 && statements[0] instanceof GrPackageDefinition) {
      packageDefinition = (GrPackageDefinition) statements[0];
    }

    if (file.isScript()) {
      VirtualFile virtualFile = file.getVirtualFile();
      assert virtualFile != null;
      String fileDefinitionName = virtualFile.getNameWithoutExtension();

      String topLevelGeneratedItemPath = createJavaSourceFile(outputRootDirectory, file, fileDefinitionName, file.getScriptClass(), packageDefinition);
      generatedItemsRelativePaths.add(topLevelGeneratedItemPath);
    }

    for (final GrTypeDefinition typeDefinition : file.getTypeDefinitions()) {
      String generatedItemPath = createJavaSourceFile(outputRootDirectory, file, typeDefinition.getName(), typeDefinition, packageDefinition);
      generatedItemsRelativePaths.add(generatedItemPath);
    }

    return generatedItemsRelativePaths;
  }

  private String getJavaClassPackage(GrPackageDefinition packageDefinition) {
    if (packageDefinition == null) return "";

    String prefix = packageDefinition.getPackageName();
    prefix = prefix.replace(".", PREFIX_SEPARATOR);
    prefix += PREFIX_SEPARATOR;

    return prefix;
  }

  private String createJavaSourceFile(VirtualFile outputRootDirectory, GroovyFileBase file, String typeDefinitionName, PsiClass typeDefinition, GrPackageDefinition packageDefinition) {
    //prefix defines structure of directories tree
    String prefix = "";
    if (packageDefinition != null) {
      prefix = getJavaClassPackage(packageDefinition);
    }

    StringBuffer text = new StringBuffer();

    writeTypeDefinition(text, typeDefinitionName, typeDefinition, packageDefinition);

    VirtualFile virtualFile = file.getVirtualFile();
    assert virtualFile != null;
//    String generatedFileRelativePath = prefix + typeDefinitionName + "." + "java";
    String fileShortName = typeDefinitionName + "." + "java";
    createGeneratedFile(text, outputRootDirectory.getPath(), prefix, fileShortName);
    return prefix + typeDefinitionName + "." + "java";
  }

  private GrTopStatement[] getTopStatementsInReadAction(final GroovyFileBase myPsiFile) {
    if (myPsiFile == null) return new GrTopStatement[0];

    return ApplicationManager.getApplication().runReadAction(new Computable<GrTopStatement[]>() {
      public GrTopStatement[] compute() {
        return myPsiFile.getTopStatements();
      }
    });
  }

  private boolean needsCreateClassFromFileName(GrTopStatement[] statements) {
    boolean isOnlyInnerTypeDef = true;
    for (GrTopStatement statement : statements) {
      if (!(statement instanceof GrTypeDefinition || statement instanceof GrImportStatement || statement instanceof GrPackageDefinition)) {
        isOnlyInnerTypeDef = false;
        break;
      }
    }
    return isOnlyInnerTypeDef;
  }

  private void writeTypeDefinition(StringBuffer text, String typeDefinitionName, PsiClass typeDefinition, GrPackageDefinition packageDefinition) {
    final boolean isScript = typeDefinition == null;

    writePackageStatement(text, packageDefinition);

    GrMembersDeclaration[] membersDeclarations = typeDefinition instanceof GrTypeDefinition ? ((GrTypeDefinition) typeDefinition).getMemberDeclarations() : GrMembersDeclaration.EMPTY_ARRAY; //todo

    boolean isClassDef = typeDefinition instanceof GrClassDefinition;
    boolean isInterface = typeDefinition instanceof GrInterfaceDefinition;


    if (typeDefinition != null) {
      PsiModifierList modifierList = typeDefinition.getModifierList();

      boolean wasAddedModifiers = modifierList != null && writeTypeDefinitionMethodModifiers(text, modifierList, JAVA_TYPE_DEFINITION_MODIFIERS, typeDefinition.isInterface());
      if (!wasAddedModifiers) {
        text.append("public ");
      }
    }

    if (isScript) {
      text.append("public ");
    }

//    text.append(" ");

    if (isInterface) text.append("interface");
    else text.append("class");

    text.append(" ");

    text.append(typeDefinitionName);

    if (typeDefinition != null && typeDefinition.hasTypeParameters()) {
      text.append("<");
      int i = 0;
      PsiTypeParameter[] parameters = typeDefinition.getTypeParameters();
      for (PsiTypeParameter parameter : parameters) {
        text.append(parameter.getName());
        if (i++ < parameters.length - 1) {
          text.append(", ");
        }
      }
      text.append(">");
    }

    text.append(" ");

    if (isScript) {
      text.append("extends ");
      text.append("groovy.lang.Script ");
    } else {
      final PsiClassType[] extendsClassesTypes = typeDefinition.getExtendsListTypes();

      if (extendsClassesTypes.length > 0) {
        text.append("extends ");
        text.append(computeTypeText(extendsClassesTypes[0]));
        text.append(" ");
      } else {
        if (isClassDef) {
          text.append("extends ");
          text.append(GrTypeDefinition.DEFAULT_BASE_CLASS_NAME);
          text.append(" ");
        }
      }

      PsiClassType[] implementsTypes = typeDefinition.getImplementsListTypes();

      if (implementsTypes.length > 0) {
        text.append(isInterface ? "extends " : "implements ");
        int i = 0;
        while (i < implementsTypes.length) {
          if (i > 0) text.append(", ");
          text.append(computeTypeText(implementsTypes[i]));
          text.append(" ");
          i++;
        }
      }
    }

    text.append("{");

    boolean wasRunMethodPresent = false;

    Set<MethodSignature> methodSignatures = new HashSet<MethodSignature>();

    PsiMethod[] methods = typeDefinition == null ? PsiMethod.EMPTY_ARRAY : typeDefinition.getMethods();
    for (PsiMethod method : methods) {
      if (method instanceof GrConstructor) {
        writeConstructor(text, (GrConstructor) method);
        continue;
      }

      PsiParameter[] parameters = method.getParameterList().getParameters();
      PsiParameter[] parametersCopy = new PsiParameter[parameters.length];
      PsiType[] parameterTypes = new PsiType[parameters.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        parametersCopy[i] = parameters[i];
        parameterTypes[i] = parameters[i].getType();
      }

      for (int i = parameters.length - 1; i >= 0; i--) {
        MethodSignature signature = MethodSignatureUtil.createMethodSignature(method.getName(), parameterTypes, method.getTypeParameters(), PsiSubstitutor.EMPTY);
        if (methodSignatures.add(signature)) {
          writeMethod(text, method, parametersCopy);
        }

        PsiParameter parameter = parameters[i];
        if (!(parameter instanceof GrParameter) || !((GrParameter) parameter).isOptional()) break;
        parameterTypes = ArrayUtil.remove(parameterTypes, parameterTypes.length - 1);
        parametersCopy = ArrayUtil.remove(parametersCopy, parametersCopy.length - 1);
      }

      wasRunMethodPresent = wasRunMethod(method);
    }

    for (GrMembersDeclaration declaration : membersDeclarations) {
      if (declaration instanceof GrVariableDeclaration) {
        writeVariableDeclarations(text, (GrVariableDeclaration) declaration);
      }
    }

    for (GrMembersDeclaration decl : membersDeclarations) {
      if (decl instanceof GrVariableDeclaration) {
        final GrVariable[] variables = ((GrVariableDeclaration) decl).getVariables();

        for (GrVariable variable : variables) {
          if (variable instanceof GrField && ((GrField) variable).isProperty()) {
            PsiMethod getter = ((GrField) variable).getGetter();
            if (getter != null) writeMethod(text, getter, getter.getParameterList().getParameters());

            PsiMethod setter = ((GrField) variable).getSetter();
            if (setter != null) writeMethod(text, setter, setter.getParameterList().getParameters());
          }
        }
      }
    }

    if (isScript && !wasRunMethodPresent) {
      writeRunMethod(text);
    }

    text.append("}");
  }

  private void getDefinedGetters(List<String> gettersNames, GrMethod method) {
    final String nameByGetter = PsiUtil.getPropertyNameByGetter(method);

    if (!nameByGetter.equals(method.getName())) {
      gettersNames.add(nameByGetter);
    }
  }

  private void getDefinedSetters(List<String> settersNames, GrMethod method) {
    final String nameBySetter = PsiUtil.getPropertyNameBySetter(method);

    if (!nameBySetter.equals(method.getName())) {
      settersNames.add(nameBySetter);
    }
  }

  private boolean wasRunMethod(PsiMethod method) {
    boolean runMethodPresent = false;
    if ("run".equals(method.getName())) {
      PsiType returnType = method.getReturnType();

      runMethodPresent = returnType != null && "java.lang.Object".equals(computeTypeText(returnType));
    }
    return runMethodPresent;
  }

  private void writePackageStatement(StringBuffer text, GrPackageDefinition packageDefinition) {
    if (packageDefinition != null) {
      text.append("package ");
      text.append(packageDefinition.getPackageName());
      text.append(";");
      text.append("\n");
      text.append("\n");
    }
  }

  private void writeRunMethod(StringBuffer text) {
    text.append("\n  public java.lang.Object run() {\n" +
        "    return null;\n" +
        "  }\n");
  }

  private void writeConstructor(final StringBuffer text, GrConstructor constructor) {
    text.append("\n");
    text.append("  ");
    writeMethodModifiers(text, constructor.getModifierList(), JAVA_MODIFIERS);

    /************* name **********/
    //append constructor name
    text.append(constructor.getName());

    /************* parameters **********/
    GrParameter[] parameterList = constructor.getParameters();

    text.append("(");
    String paramType;
    GrTypeElement paramTypeElement;

    for (int i = 0; i < parameterList.length; i++) {
      if (i > 0) text.append(", ");

      GrParameter parameter = parameterList[i];
      paramTypeElement = parameter.getTypeElementGroovy();
      paramType = getTypeText(paramTypeElement);

      text.append(paramType);
      text.append(" ");
      text.append(parameter.getName());
    }

    text.append(")");
    text.append(" ");

    /************* body **********/

    final GrConstructorInvocation constructorInvocation = constructor.getChainingConstructorInvocation();
    if (constructorInvocation != null && constructorInvocation.isSuperCall()) {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          PsiMethod superConstructor = constructorInvocation.resolveConstructor();
          if (superConstructor == null) {
            final GroovyResolveResult[] results = constructorInvocation.multiResolveConstructor();
            if (results.length > 0) {
              superConstructor = (PsiMethod) results[0].getElement();
            }
          }

          if (superConstructor != null) {
            final PsiClassType[] throwsTypes = superConstructor.getThrowsList().getReferencedTypes();
            if (throwsTypes.length > 0) {
              text.append(" throws ");
              for (int i = 0; i < throwsTypes.length; i++) {
                if (i > 0) text.append(", ");
                text.append(getTypeText(throwsTypes[i]));
              }
            }
          }

          text.append("{\n");

          text.append("    ");
          text.append("super");
          text.append("(");

          if (superConstructor != null) {
            final PsiParameter[] superParams = superConstructor.getParameterList().getParameters();
            for (int i = 0; i < superParams.length; i++) {
              if (i > 0) text.append(", ");
              String typeText = getTypeText(superParams[i].getType());
              text.append("(" + typeText + ")" + getDefaultValueText(typeText));
            }
          }

          text.append(")");
          text.append(";");
        }
      });

    } else {
      text.append("{\n");
    }

    text.append("\n  }");
    text.append("\n");
  }

  private String getDefaultValueText(String typeCanonicalText) {
    final String result = typesToInitialValues.get(typeCanonicalText);
    if (result == null) return "null";
    return result;
  }

  private void writeVariableDeclarations(StringBuffer text, GrVariableDeclaration variableDeclaration) {
    GrTypeElement varTypeElement = variableDeclaration.getTypeElementGroovy();
    String varQualifiedTypeName = getTypeText(varTypeElement);

    String initValueText = getDefaultValueText(varQualifiedTypeName);

    //append method name
    PsiModifierList modifierList = variableDeclaration.getModifierList();
    GrVariable[] grVariables = variableDeclaration.getVariables();
    GrVariable variable;
    int i = 0;
    while (i < grVariables.length) {
      variable = grVariables[i];

      text.append("\n");
      text.append("  ");
      writeVariableDefinitionModifiers(text, modifierList, JAVA_MODIFIERS);

      //type
      text.append(varQualifiedTypeName);
      text.append(" ");

      //var name
      text.append(variable.getName());
      text.append(" = ");

      text.append(initValueText);
      text.append(";");
      text.append("\n");
      i++;
    }
  }

  private void writeMethod(StringBuffer text, PsiMethod method, final PsiParameter[] parameters) {
    if (method == null) return;
    boolean isAbstract = method.hasModifierProperty(PsiModifier.ABSTRACT);

    PsiModifierList modifierList = method.getModifierList();

    text.append("\n");
    text.append("  ");
    writeMethodModifiers(text, modifierList, JAVA_MODIFIERS);

    //append return type
    PsiType retType;
    if (method instanceof GrMethod) {
      retType = ((GrMethod) method).getDeclaredReturnType();
      if (retType == null) retType = TypesUtil.createJavaLangObject(method);
    }
    else retType = method.getReturnType();

    text.append(getTypeText(retType));
    text.append(" ");

    //append method name
    text.append(method.getName());

    /************* parameters **********/

    text.append("(");

    //writes parameters
    int i = 0;
    while (i < parameters.length) {
      PsiParameter parameter = parameters[i];
      if(parameter == null) continue;

      if (i > 0) text.append(", ");  //append ','

      text.append(getTypeText(parameter.getType()));
      text.append(" ");
      text.append(parameter.getName());

      i++;
    }
    text.append(")");
    text.append(" ");

    if (!isAbstract) {
      /************* body **********/
      text.append("{\n");
      text.append("    return ");

      text.append(getDefaultValueText(getTypeText(method.getReturnType())));

      text.append(";");

      text.append("\n  }");
    } else {
      text.append(";");
    }
    text.append("\n");
  }

  private boolean writeMethodModifiers(StringBuffer text, PsiModifierList modifierList, String[] modifiers) {
    boolean wasAddedModifiers = false;
    for (String modifierType : modifiers) {
      if (modifierList.hasModifierProperty(modifierType)) {
        text.append(modifierType);
        text.append(" ");
        wasAddedModifiers = true;
      }
    }
    return wasAddedModifiers;
  }

  private boolean writeVariableDefinitionModifiers(StringBuffer text, PsiModifierList modifierList, String[] modifiers) {
    boolean wasAddedModifiers = false;
    for (String modifierType : modifiers) {
      if (modifierList.hasModifierProperty(modifierType)) {
        text.append(modifierType);
        text.append(" ");
        wasAddedModifiers = true;
      }
    }
    return wasAddedModifiers;
  }

  private boolean writeTypeDefinitionMethodModifiers(StringBuffer text, PsiModifierList modifierList, String[] modifiers, boolean isInterface) {
    boolean wasAddedModifiers = false;
    for (String modifierType : modifiers) {
      if (modifierList.hasModifierProperty(modifierType)) {
        if (PsiModifier.ABSTRACT.equals(modifierType) && isInterface) {
          continue;
        }
        text.append(modifierType);
        text.append(" ");
        wasAddedModifiers = true;
      }
    }
    return wasAddedModifiers;
  }

  private String getTypeText(GrTypeElement typeElement) {
    if (typeElement == null) {
      return "java.lang.Object";
    } else {
      return computeTypeText(typeElement.getType());
    }
  }

  private String getTypeText(PsiType type) {
    if (type == null) {
      return "java.lang.Object";
    } else {
      return computeTypeText(type);
    }
  }

  private String computeTypeText(PsiType type) {
    if (type instanceof PsiArrayType) {
      String componentText = computeTypeText(((PsiArrayType) type).getComponentType());
      if (type instanceof PsiEllipsisType) return componentText + "...";
      return componentText + "[]";
    }

    String canonicalText = type.getCanonicalText();
    return canonicalText != null ? canonicalText : type.getPresentableText();
  }

  private void createGeneratedFile(StringBuffer text, String outputDir, String prefix, String generatedItemPath) {
    assert prefix != null;

    String prefixWithoutSeparator = prefix;

    if (!"".equals(prefix)) {
      prefixWithoutSeparator = prefix.substring(0, prefix.length() - PREFIX_SEPARATOR.length());
      new File(outputDir, prefixWithoutSeparator).mkdirs();
    }

    File myFile;
    if (!"".equals(prefix))
      myFile = new File(outputDir + File.separator + prefixWithoutSeparator, generatedItemPath);
    else
      myFile = new File(outputDir, generatedItemPath);

    BufferedWriter writer = null;
    try {
      Writer fileWriter = new FileWriter(myFile);
      writer = new BufferedWriter(fileWriter);
      writer.write(text.toString());
    } catch (IOException e) {
      LOG.error(e);
    } finally {
      try {
        assert writer != null;
        writer.close();
      } catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @NotNull
  public String getDescription() {
    return "Groovy to java source code generator";
  }

  public boolean validateConfiguration(CompileScope scope) {
//    scope.getFiles(GroovyFileType.GROOVY_FILE_TYPE, true);
    return true;
  }

  public ValidityState createValidityState(DataInputStream is) throws IOException {
    return TimestampValidityState.load(is);
  }

  CharTrie myTrie = new CharTrie();

  public void compilationFinished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
    myTrie.clear();
  }

  class GenerationItemImpl implements GenerationItem {
    ValidityState myState;
    private boolean myInTestSources;
    final Module myModule;
    public int myHashCode;
    private VirtualFile myVFile;

    public GenerationItemImpl(String path, Module module, ValidityState state, boolean isInTestSources, VirtualFile vFile) {
      myVFile = vFile;
      myModule = module;
      myState = state;
      myInTestSources = isInTestSources;
      myHashCode = myTrie.getHashCode(path);
    }

    public String getPath() {
      return myTrie.getString(myHashCode);
    }

    public ValidityState getValidityState() {
      return myState;
    }

    public Module getModule() {
      return myModule;
    }

    public boolean isTestSource() {
      return myInTestSources;
    }

    public VirtualFile getVFile() {
      return myVFile;
    }
  }
}