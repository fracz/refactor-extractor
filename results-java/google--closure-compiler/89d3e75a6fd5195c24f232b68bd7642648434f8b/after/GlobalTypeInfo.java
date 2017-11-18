/*
 * Copyright 2013 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.jscomp.newtypes.DeclaredFunctionType;
import com.google.javascript.jscomp.newtypes.DeclaredTypeRegistry;
import com.google.javascript.jscomp.newtypes.FunctionTypeBuilder;
import com.google.javascript.jscomp.newtypes.JSType;
import com.google.javascript.jscomp.newtypes.JSTypeCreatorFromJSDoc;
import com.google.javascript.jscomp.newtypes.NominalType;
import com.google.javascript.jscomp.newtypes.ObjectType;
import com.google.javascript.jscomp.newtypes.TypeUtils;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains information about all scopes; for every variable reference computes
 * whether it is local, a formal parameter, etc.; and computes information about
 * the class hierarchy.
 *
 * Under development. DO NOT USE!
 *
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
class GlobalTypeInfo {

  static final DiagnosticType DUPLICATE_JSDOC = DiagnosticType.warning(
      "JSC_DUPLICATE_JSDOC",
      "Found two JsDoc comments for variable: {0}.\n");

  static final DiagnosticType DUPLICATE_PROPERTY_JSDOC = DiagnosticType.warning(
      "JSC_DUPLICATE_PROPERTY_JSDOC",
      "Found two JsDoc comments for property {0} of type {1}.\n");

  static final DiagnosticType INVALID_PROP_OVERRIDE = DiagnosticType.warning(
      "JSC_INVALID_PROP_OVERRIDE",
      "Invalid redeclaration of property {0}.\n" +
      "inherited type  : {1}\n" +
      "overriding type : {2}\n");

  static final DiagnosticType EXTENDS_NOT_ON_CTOR_OR_INTERF =
      DiagnosticType.warning(
          "JSC_EXTENDS_NOT_ON_CTOR_OR_INTERF",
          "@extends used without @constructor or @interface for {0}.\n");

  static final DiagnosticType EXTENDS_NON_OBJECT = DiagnosticType.warning(
      "JSC_EXTENDS_NON_OBJECT",
      "{0} extends non-object type {1}.\n");

  static final DiagnosticType CTOR_IN_DIFFERENT_SCOPE = DiagnosticType.warning(
      "JSC_CTOR_IN_DIFFERENT_SCOPE",
      "Modifying the prototype is only allowed if the constructor is " +
      "in the same scope\n");

  static final DiagnosticType UNRECOGNIZED_TYPE_NAME = DiagnosticType.warning(
      "JSC_UNRECOGNIZED_TYPE_NAME",
      "Type annotation references non-existent type {0}.");

  static final DiagnosticType INTERFACE_WITH_A_BODY = DiagnosticType.warning(
      "JSC_INTERFACE_WITH_A_BODY",
      "Interface definitions should have an empty body.");

  // Invariant: if a scope s1 contains a scope s2, then s2 is before s1 in
  // scopes. The type inference relies on this fact to process deeper scopes
  // before shallower scopes.
  private final Deque<Scope> scopes = Lists.newLinkedList();
  private final Deque<Scope> scopeWorkset = Lists.newLinkedList();
  private final Set<JSError> warnings = Sets.newHashSet();
  private final JSTypeCreatorFromJSDoc typeParser =
      new JSTypeCreatorFromJSDoc();
  private final AbstractCompiler compiler;
  private final Map<Node, String> anonFunNames = Maps.newHashMap();
  private int funCounter = 1;
  private Map<Node, NominalType> nominaltypesByNode = Maps.newHashMap();
  private HashBasedTable<NominalType, String, PropertyDef> propertyDefs =
      HashBasedTable.create();

  GlobalTypeInfo(AbstractCompiler compiler) {
    this.compiler = compiler;
  }

  public Collection<Scope> getScopes() {
    return scopes;
  }

  public Collection<JSError> getWarnings() {
    return warnings;
  }

  // Differs from the similar method in Scope class on how it treats qnames.
  public String getFunInternalName(Node n) {
    Preconditions.checkState(n.isFunction());
    String nonAnonFnName = NodeUtil.getFunctionName(n);
    // We don't want to use qualified names here
    if (nonAnonFnName != null && TypeUtils.isIdentifier(nonAnonFnName)) {
      return nonAnonFnName;
    }
    return anonFunNames.get(n);
  }

  void prepareAst(Node externs, Node root) {
    Preconditions.checkArgument(externs.isSyntheticBlock());
    Preconditions.checkArgument(root.isSyntheticBlock());
    Scope rootScope = new Scope(root, null, new ArrayList<String>(), null);
    scopes.addFirst(rootScope);

    CollectNamedTypes rootCnt = new CollectNamedTypes(rootScope);
    new NodeTraversal(compiler, rootCnt).traverse(externs);
    new NodeTraversal(compiler, rootCnt).traverse(root);
    ProcessScope rootPs = new ProcessScope(rootScope);
    new NodeTraversal(compiler, rootPs).traverse(externs);
    new NodeTraversal(compiler, rootPs).traverse(root);
    rootPs.finishProcessingScope();

    // loop through the workset (outer-to-inner scopes)
    while (!scopeWorkset.isEmpty()) {
      Scope s = scopeWorkset.removeFirst();
      Node scopeBody = s.getBody();
      new NodeTraversal(compiler, new CollectNamedTypes(s)).traverse(scopeBody);
      ProcessScope ps = new ProcessScope(s);
      new NodeTraversal(compiler, ps).traverse(scopeBody);
      ps.finishProcessingScope();
    }

    // Report errors in the inheritance chain, after we're done constructing it.
    reportInheritanceErrors();

    nominaltypesByNode = null;
    propertyDefs = null;
    for (Scope s : scopes) {
      s.finalizeScope();
    }
    // The jsdoc parser doesn't have access to the error functions in the jscomp
    // package, so we collect its warnings here.
    for (String warningText : typeParser.getWarnings()) {
      // TODO(blickly): Make warnings better
      warnings.add(JSError.make(
          root, RhinoErrorReporter.BAD_JSDOC_ANNOTATION, warningText));
    }
  }

  /** Report all errors that must be checked at the end of GlobalTypeInfo */
  private void reportInheritanceErrors() {
    Deque<Node> workset = Lists.newLinkedList(nominaltypesByNode.keySet());
  workset_loop:
    while (!workset.isEmpty()) {
      Node funNode = workset.removeFirst();
      NominalType nominalType = nominaltypesByNode.get(funNode);
      NominalType superClass = nominalType.getSuperClass();
      if (superClass != null && !superClass.isFinalized()) {
        workset.addLast(funNode);
        continue workset_loop;
      }
      for (NominalType superInterf : nominalType.getInterfaces()) {
        if (!superInterf.isFinalized()) {
          workset.addLast(funNode);
          continue workset_loop;
        }
      }

      // Detect bad inheritance for extended classes
      if (superClass != null) {
        // TODO(blickly): Can we optimize this to skip unnecessary iterations?
        for (String pname : superClass.getAllPropsOfClass()) {
          PropertyDef propDef = propertyDefs.get(nominalType, pname);
          PropertyDef inheritedPropDef = propertyDefs.get(superClass, pname);
          if (inheritedPropDef != null &&
              inheritedPropDef.methodScope != null) {
            // TODO(blickly): Save DeclaredFunctionTypes somewhere other than
            // inside Scopes so that we can access them even for methods that
            // do not create a Scope at definition site.
            DeclaredFunctionType propDeclType;
            if (propDef == null || propDef.methodScope == null) {
              propDeclType = inheritedPropDef.methodScope.getDeclaredType();
            } else {
              DeclaredFunctionType funDeclType =
                  propDef.methodScope.getDeclaredType();
              DeclaredFunctionType inheritedFunDeclType =
                  inheritedPropDef.methodScope.getDeclaredType();
              propDeclType =
                  funDeclType.withTypeInfoFromSuper(inheritedFunDeclType);
              propDef.methodScope.setDeclaredType(propDeclType);
            }
            nominalType.addProtoProperty(pname,
                JSType.fromFunctionType(propDeclType.toFunctionType()));
          }
          JSType inheritedPropType = superClass.getPropDeclaredType(pname);
          if (inheritedPropType == null) {
            // No need to go further for undeclared props.
            continue;
          }
          JSType localPropType = nominalType.getPropDeclaredType(pname);
          // System.out.println("nominalType: " + nominalType + "." + pname +
          //     " inheritedPropType: " + inheritedPropType +
          //  " localPropType: " + localPropType);
          if (!localPropType.isSubtypeOf(inheritedPropType)) {
            warnings.add(JSError.make(
                propertyDefs.get(nominalType, pname).defSite,
                INVALID_PROP_OVERRIDE, pname,
                inheritedPropType.toString(), localPropType.toString()));
          }
        }
      }

      // Detect bad inheritance for extended/implemented interfaces
      for (NominalType superInterf: nominalType.getInterfaces()) {
        for (String pname : superInterf.getAllPropsOfInterface()) {
          PropertyDef inheritedPropDef = propertyDefs.get(superInterf, pname);
          JSType inheritedPropType = superInterf.getPropDeclaredType(pname);
          if (!nominalType.mayHaveProp(pname)) {
            warnings.add(JSError.make(inheritedPropDef.defSite,
                    TypeValidator.INTERFACE_METHOD_NOT_IMPLEMENTED,
                    pname, superInterf.toString(), nominalType.toString()));
            continue;
          }
          PropertyDef propDef = propertyDefs.get(nominalType, pname);
          JSType localPropType = nominalType.getPropDeclaredType(pname);
          if (localPropType != null &&
              !localPropType.isSubtypeOf(inheritedPropType)) {
            warnings.add(JSError.make(
                propDef.defSite, INVALID_PROP_OVERRIDE, pname,
                inheritedPropType.toString(), localPropType.toString()));
          } else if (nominalType.isClass()) {
            if (localPropType == null) {
              // Add property from interface to class
              nominalType.addProtoProperty(pname, inheritedPropType);
            } else if (propDef.methodScope != null) {
              // If we are looking at a method definition, munging may be needed
              DeclaredFunctionType propDeclType =
                  propDef.methodScope.getDeclaredType();
              propDeclType = propDeclType.withTypeInfoFromSuper(
                  inheritedPropDef.methodScope.getDeclaredType());
              propDef.methodScope.setDeclaredType(propDeclType);
              nominalType.addProtoProperty(pname,
                  JSType.fromFunctionType(propDeclType.toFunctionType()));
            }
          }
        }
      }

      // Finalize nominal type once all properties are added.
      nominalType.finalizeNominalType();
    }
  }

  /**
   * Create scopes for functions within the given function.
   * This involves naming the function, finding the formals, creating
   * a new scope, and if it is a constructur, creating a new NominalType.
   */
  private class CollectNamedTypes extends AbstractShallowCallback {
    private final Scope currentScope;

    CollectNamedTypes(Scope s) {
      this.currentScope = s;
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isFunction()) {
        initFnScope(n, currentScope);
      }
    }

    private void initFnScope(Node fn, Scope parentScope) {
      String qname = NodeUtil.getFunctionName(fn);
      // Qualified names also need a gensymed name.
      if (qname == null || !TypeUtils.isIdentifier(qname)) {
        anonFunNames.put(fn, "%anon_fun" + funCounter);
        funCounter++;
      }
      JSDocInfo fnDoc = NodeUtil.getFunctionJSDocInfo(fn);
      if (fnDoc != null && (fnDoc.isConstructor() || fnDoc.isInterface())) {
        NominalType klass = new NominalType(qname, fnDoc.isInterface());
        nominaltypesByNode.put(fn, klass);
        parentScope.addClassType(qname, klass);
      }
    }
  }

  private JSType getTypeDeclarationFromJsdoc(Node n, Scope s) {
    return typeParser.getNodeTypeDeclaration(n.getJSDocInfo(), s);
  }

  private class ProcessScope extends AbstractShallowCallback {
    private final Scope currentScope;
    /**
     * Keep track of undeclared vars as they are crawled to warn about
     * use before declaration and undeclared variables.
     * We use a multimap so we can give all warnings rather than just the first.
     */
    private final Multimap<String, Node> undeclaredVars;

    ProcessScope(Scope currentScope) {
      this.currentScope = currentScope;
      this.undeclaredVars = HashMultimap.create();
    }

    private JSType getVarTypeFromAnnotation(Node nameNode) {
      Preconditions.checkArgument(nameNode.getParent().isVar());
      Node varNode = nameNode.getParent();
      JSType varType = getTypeDeclarationFromJsdoc(varNode, currentScope);
      if (varNode.getChildCount() > 1 && varType != null) {
        warnings.add(JSError.make(varNode, TypeCheck.MULTIPLE_VAR_DEF));
      }
      String varName = nameNode.getQualifiedName();
      JSType nameNodeType = getTypeDeclarationFromJsdoc(nameNode, currentScope);
      if (nameNodeType != null) {
        if (varType != null) {
          warnings.add(JSError.make(nameNode, DUPLICATE_JSDOC, varName));
        }
        return nameNodeType;
      } else {
        return varType;
      }
    }

    void finishProcessingScope() {
      for (Node nameNode : undeclaredVars.values()) {
        warnings.add(JSError.make(nameNode,
              VarCheck.UNDEFINED_VAR_ERROR, nameNode.getQualifiedName()));
      }
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.FUNCTION:
          Node grandparent = parent.getParent();
          if (grandparent == null ||
              (!grandparent.isVar() &&
                  !NodeUtil.isPrototypePropertyDeclaration(grandparent))) {
            visitFunctionDef(n);
          }
          break;
        case Token.NAME: {
          String name = n.getQualifiedName();
          if (name == null || "undefined".equals(name) || parent.isFunction()) {
            return;
          }
          if (parent.isVar()) {
            if (currentScope.isDefinedLocally(name)) {
              warnings.add(JSError.make(
                  n, VariableReferenceCheck.REDECLARED_VARIABLE, name));
            } else {
              for (Node useBeforeDeclNode : undeclaredVars.get(name)) {
                warnings.add(JSError.make(useBeforeDeclNode,
                    VariableReferenceCheck.UNDECLARED_REFERENCE, name));
              }
              undeclaredVars.removeAll(name);
              Node initializer = n.getFirstChild();
              if (initializer != null && initializer.isFunction()) {
                visitFunctionDef(initializer);
              } else if (initializer != null &&
                  NodeUtil.isNamespaceDecl(n)) {
                currentScope.addNamespace(name);
              } else {
                currentScope.addLocal(name, getVarTypeFromAnnotation(n));
              }
            }
          } else if (currentScope.isOuterVarEarly(name)) {
            currentScope.addOuterVar(name);
          } else if (!currentScope.isDefinedLocally(name)) {
            undeclaredVars.put(name, n);
          }
          break;
        }
        case Token.GETPROP:
          // Prototype property
          if (NodeUtil.isPrototypeProperty(n) && parent.isExprResult()) {
            visitPrototypePropertyDeclaration(parent);
          }
          break;
        case Token.ASSIGN: {
          Node lvalue = n.getFirstChild();
          if (!lvalue.isGetProp()) {
            return;
          }

          // Class property
          if (lvalue.getFirstChild().isThis() &&
              (currentScope.isConstructor() ||
                  currentScope.isPrototypeMethod())) {
            visitClassPropertyDeclaration(lvalue);
            return;
          }

          // Prototype property
          if (NodeUtil.isPrototypePropertyDeclaration(parent)) {
            visitPrototypePropertyDeclaration(parent);
            return;
          }

          // "Static" property on constructor
          String receiverObjName = lvalue.getFirstChild().getQualifiedName();
          if (currentScope.isLocalFunDef(receiverObjName) &&
              currentScope.getScope(receiverObjName)
              .getDeclaredType().getClassType() != null) {
            visitConstructorPropertyDeclaration(n);
            return;
          }

          // Namespace property
          if (receiverObjName != null &&
              currentScope.isNamespace(receiverObjName)) {
            visitNamespacePropertyDeclaration(n);
          }

          break;
        }
      }
    }

    /** Returns the newly created scope for this function */
    private Scope visitFunctionDef(Node fn) {
      Preconditions.checkArgument(fn.isFunction());
      Scope fnScope = computeFnDeclaredType(fn, currentScope);
      scopes.addFirst(fnScope);
      String fnName = NodeUtil.getFunctionName(fn);
      String internalName = getFunInternalName(fn);
      if (currentScope.isDefinedLocally(fnName)) {
        warnings.add(JSError.make(
            fn, VariableReferenceCheck.REDECLARED_VARIABLE, fnName));
      } else {
        currentScope.putLocalFunDef(internalName, fnScope);
        if (fnName != null && !TypeUtils.isIdentifier(fnName)) {
          // Qualified names will be removed in finalizeScope
          currentScope.putLocalFunDef(fnName, fnScope);
        }
      }
      if (fnName != null && TypeUtils.isIdentifier(fnName)) {
        undeclaredVars.removeAll(fnName);
      }
      scopeWorkset.add(fnScope);
      return fnScope;
    }

    private void visitPrototypePropertyDeclaration(Node exprResult) {
      Node expr = exprResult.getFirstChild();
      Node getPropNode, initializer;
      if (expr.isAssign()) {
        getPropNode = expr.getFirstChild();
        initializer = expr.getLastChild();
      } else {
        getPropNode = expr;
        initializer = null;
      }
      Node ctorNameNode = NodeUtil.getPrototypeClassName(getPropNode);
      String ctorName = ctorNameNode.getQualifiedName();

      // We only add properties to the prototype of a class if the
      // property creations are in the same scope as the constructor
      if (currentScope.isDefinedLocally(ctorName)) {
        NominalType classType = currentScope.getNominalType(ctorName);
        if (classType == null) {
          // We don't look at assignments to prototypes of non-constructors.
          return;
        }
        String pname = NodeUtil.getPrototypePropertyName(getPropNode);
        // Find the declared type of the property.
        JSType propDeclType;
        Scope methodScope = null;
        if (initializer != null && initializer.isFunction()) {
          // TODO(user): we must do this for any function "defined" as the rhs
          // of an assignment to a property, not just when the property is a
          // prototype property.
          methodScope = visitFunctionDef(initializer);
          propDeclType = JSType.fromFunctionType(
              methodScope.getDeclaredType().toFunctionType());
        } else {
          propDeclType = getTypeDeclarationFromJsdoc(expr, currentScope);
        }
        propertyDefs.put(classType, pname,
            new PropertyDef(exprResult, methodScope));
        // Add the property to the class with the appropriate type.
        if (propDeclType != null) {
          if (mayWarnAboutExistingProp(
              classType, pname, propDeclType, expr)) {
            return;
          }
          classType.addProtoProperty(pname, propDeclType);
        } else {
          classType.addUndeclaredProtoProperty(pname);
        }
      } else {
        warnings.add(JSError.make(expr, CTOR_IN_DIFFERENT_SCOPE));
      }
    }

    private void visitConstructorPropertyDeclaration(Node assignNode) {
      Node getPropNode = assignNode.getFirstChild();
      String ctorName = getPropNode.getFirstChild().getQualifiedName();
      Preconditions.checkState(currentScope.isLocalFunDef(ctorName));
      NominalType classType = currentScope.getNominalType(ctorName);
      String pname = getPropNode.getLastChild().getString();
      JSType propDeclType =
          getTypeDeclarationFromJsdoc(assignNode, currentScope);
      if (propDeclType != null) {
       if (classType.mayHaveCtorProp(pname) &&
           classType.getCtorPropDeclaredType(pname) != null) {
         warnings.add(JSError.make(assignNode, DUPLICATE_PROPERTY_JSDOC,
               pname, classType.toString()));
         return;
       }
       classType.addCtorProperty(pname, propDeclType);
     } else {
       classType.addUndeclaredCtorProperty(pname);
     }
    }

    private void visitNamespacePropertyDeclaration(Node assignNode) {
      Node lvalue = assignNode.getFirstChild();
      String qname = lvalue.getQualifiedName();
      String leftmost = TypeUtils.getQnameRoot(qname);
      JSType declType = getTypeDeclarationFromJsdoc(assignNode, currentScope);
      JSType newType = currentScope.getDeclaredTypeOf(leftmost);
      if (declType == null) {
        newType = newType.withProperty(
            TypeUtils.getPropPath(qname), JSType.UNKNOWN);
      } else {
        newType = newType.withDeclaredProperty(
            TypeUtils.getPropPath(qname), declType);
      }
      currentScope.updateTypeOfLocal(leftmost, newType);
    }

    private void visitClassPropertyDeclaration(Node getPropNode) {
      NominalType thisType = currentScope.getDeclaredType().getThisType();
      String pname = getPropNode.getLastChild().getString();
      // TODO(blickly): Support @param, @return style fun declarations here.
      JSType declaredType =
          getTypeDeclarationFromJsdoc(getPropNode.getParent(), currentScope);
      if (declaredType != null) {
        mayWarnAboutExistingProp(thisType, pname, declaredType, getPropNode);
        thisType.addClassProperty(pname, declaredType);
      } else {
        thisType.addUndeclaredClassProperty(pname);
      }
      propertyDefs.put(thisType, pname,
          new PropertyDef(getPropNode.getParent().getParent(), null));
    }

    boolean mayWarnAboutExistingProp(NominalType classType, String pname,
        JSType propTypeFromJsdoc, Node propCreationNode) {
      if (classType.mayHaveOwnProp(pname) &&
          classType.getPropDeclaredType(pname) != null) {
        warnings.add(JSError.make(propCreationNode, DUPLICATE_PROPERTY_JSDOC,
                pname, classType.toString()));
        return true;
      }
      return false;
    }

    /**
     * Compute the declared type for a given scope.
     */
    private Scope computeFnDeclaredType(Node fn, Scope parentScope) {
      Preconditions.checkArgument(fn.isFunction());
      JSDocInfo fnDoc = NodeUtil.getFunctionJSDocInfo(fn);

      // TODO(user): warn if multiple jsdocs for a fun

      // Collect the names of the formals
      ArrayList<String> formals = Lists.newArrayList();
      for (Node param = NodeUtil.getFunctionParameters(fn).getFirstChild();
           param != null; param = param.getNext()) {
        formals.add(param.getQualifiedName());
      }
      // Compute the types of formals and the return type
      FunctionTypeBuilder builder =
          typeParser.getFunctionType(fnDoc, fn, parentScope);

      // Look at other annotations, eg, @constructor
      String functionName = getFunInternalName(fn);
      if (fnDoc != null) {
        NominalType parentClass = null;
        if (fnDoc.hasBaseType()) {
          if (!fnDoc.isConstructor()) {
            warnings.add(JSError.make(
                fn, EXTENDS_NOT_ON_CTOR_OR_INTERF, functionName));
          } else {
            Node docNode = fnDoc.getBaseType().getRootNode();
            if (typeParser.hasKnownType(docNode, parentScope)) {
              parentClass = typeParser.getClassType(docNode, parentScope);
              if (parentClass == null) {
                warnings.add(JSError.make(fn, EXTENDS_NON_OBJECT, functionName,
                      docNode.toStringTree()));
              }
            } else {
              warnings.add(JSError.make(
                    fn, UNRECOGNIZED_TYPE_NAME, docNode.toStringTree()));
            }
          }
        }
        NominalType nominalType = nominaltypesByNode.get(fn);
        if (fnDoc.isConstructor()) {
          if (parentClass != null) {
            nominalType.addSuperClass(parentClass);
          }
          nominalType.addInterfaces(
              typeParser.getImplementedInterfaces(fnDoc, parentScope));
          builder.addClass(nominalType);
        } else if (fnDoc.isInterface()) {
          if (!NodeUtil.isEmptyBlock(NodeUtil.getFunctionBody(fn))) {
            warnings.add(JSError.make(fn, INTERFACE_WITH_A_BODY));
          }
          nominalType.addInterfaces(
              typeParser.getExtendedInterfaces(fnDoc, parentScope));
          builder.addClass(nominalType);
        }
      }

      if (NodeUtil.isPrototypeMethod(fn)) {
        Node lhsNode = fn.getParent().getFirstChild();
        String className =
            NodeUtil.getPrototypeClassName(lhsNode).getQualifiedName();
        builder.addReceiverType(parentScope.getScope(className)
            .getDeclaredType().getClassType());
      }
      return new Scope(fn, parentScope, formals, builder.buildDeclaration());
    }
  }

  static class PropertyDef {
    Node defSite; // The expression result of the property definition
    Scope methodScope; // null for non-method property declarations

    PropertyDef(Node defSite, Scope methodScope) {
      this.defSite = defSite;
      this.methodScope = methodScope;
    }
  }

  static class Scope implements DeclaredTypeRegistry {
    private final Scope parent;
    private final Node root;
    private final String name; // null for top scope

    private final Map<String, JSType> locals = Maps.newHashMap();
    private final ArrayList<String> formals;
    private final Set<String> outerVars = Sets.newHashSet();
    private final Map<String, Scope> localFunDefs = Maps.newHashMap();
    private Map<String, NominalType> localClassDefs = Maps.newHashMap();
    private Set<String> localNamespaces = Sets.newHashSet();

    // declaredType is null for top level, but never null for functions,
    // even those without jsdoc.
    // Any inferred parameters or return will be set to null individually.
    private DeclaredFunctionType declaredType;

    private Scope(Node root, Scope parent, ArrayList<String> formals,
        DeclaredFunctionType declaredType) {
      this.name =
          parent == null ? null : NodeUtil.getFunctionName(root);
      this.root = root;
      this.parent = parent;
      this.formals = formals;
      this.declaredType = declaredType;
    }

    Node getRoot() {
      return root;
    }

    private Node getBody() {
      if (root.isFunction()) {
        return NodeUtil.getFunctionBody(root);
      } else { // top-level scope
        return root;
      }
    }

    // TODO(user): don't return null for anonymous functions
    public String getReadableName() {
      return name;
    }

    private void setDeclaredType(DeclaredFunctionType declaredType) {
      this.declaredType = declaredType;
    }

    public DeclaredFunctionType getDeclaredType() {
      return declaredType;
    }

    public boolean isFunction() {
      return root.isFunction();
    }

    public boolean isConstructor() {
      if (!root.isFunction()) {
        return false;
      }
      JSDocInfo fnDoc = NodeUtil.getFunctionJSDocInfo(root);
      return fnDoc != null && fnDoc.isConstructor();
    }

    public boolean isPrototypeMethod() {
      Preconditions.checkState(root != null);
      return NodeUtil.isPrototypeMethod(root);
    }

    private void putLocalFunDef(String name, Scope scope) {
      localFunDefs.put(name, scope);
    }

    boolean isFormalParam(String name) {
      return formals.contains(name);
    }

    boolean isLocalVar(String name) {
      return locals.containsKey(name);
    }

    boolean isLocalFunDef(String name) {
      return localFunDefs.containsKey(name);
    }

    boolean isDefinedLocally(String name) {
      return locals.containsKey(name) || formals.contains(name) ||
          localFunDefs.containsKey(name) || "this".equals(name);
    }

    boolean isNamespace(String name) {
      return localNamespaces.contains(name);
    }

    boolean isVisibleInScope(String name) {
      return isDefinedLocally(name) ||
          (parent != null && parent.isVisibleInScope(name));
    }

    private boolean isOuterVarEarly(String name) {
      return !isDefinedLocally(name) && isVisibleInScope(name);
    }

    boolean isUndeclaredFormal(String name) {
      return formals.contains(name) && getDeclaredTypeOf(name) == null;
    }

    List<String> getFormals() {
      return Lists.newArrayList(formals);
    }

    Set<String> getOuterVars() {
      return Sets.newHashSet(outerVars);
    }

    Set<String> getLocalFunDefs() {
      return Sets.newHashSet(localFunDefs.keySet());
    }

    boolean isOuterVar(String name) {
      return outerVars.contains(name);
    }

    boolean hasThis() {
      if (isFunction() && getDeclaredType().getThisType() != null) {
        return true;
      }
      return false;
    }

    NominalType getNominalType(String name) {
      Preconditions.checkState(localClassDefs != null);
      NominalType klass = localClassDefs.get(name);
      if (klass != null) {
        return klass;
      }
      if (parent != null) {
        return parent.getNominalType(name);
      }
      return null;
    }

    @Override
    public JSType getNominalTypeAsJstype(String name) {
      Preconditions.checkState(localClassDefs != null);
      NominalType klass = getNominalType(name);
      if (klass != null) {
        return JSType.join(
            JSType.fromObjectType(ObjectType.fromClass(klass)), JSType.NULL);
      }
      return null;
    }

    public JSType getDeclaredTypeOf(String name) {
      if ("this".equals(name)) {
        if (!hasThis()) {
          return null;
        }
        return JSType.fromObjectType(ObjectType.fromClass(
            getDeclaredType().getThisType()));
      }
      int formalIndex = formals.indexOf(name);
      if (formalIndex != -1) {
        JSType formalType = declaredType.getFormalType(formalIndex);
        if (formalType != null && formalType.isBottom()) {
          return null;
        }
        return formalType;
      }
      JSType localType = locals.get(name);
      if (localType != null) {
        return localType;
      }
      Scope s = localFunDefs.get(name);
      if (s != null) {
        return JSType.fromFunctionType(s.getDeclaredType().toFunctionType());
      }
      if (parent != null) {
        return parent.getDeclaredTypeOf(name);
      }
      return null;
    }

    private Scope getScopeHelper(String fnName) {
      Scope s = localFunDefs.get(fnName);
      if (s != null) {
        return s;
      } else if (parent != null) {
        return parent.getScopeHelper(fnName);
      }
      return null;
    }

    boolean isKnownFunction(String fnName) {
      return getScopeHelper(fnName) != null;
    }

    Scope getScope(String fnName) {
      Scope s = getScopeHelper(fnName);
      Preconditions.checkState(s != null);
      return s;
    }

    Set<String> getLocals() {
      return ImmutableSet.copyOf(locals.keySet());
    }

    void addLocal(String name, JSType declType) {
      Preconditions.checkState(!isDefinedLocally(name));
      locals.put(name, declType);
    }

    void addNamespace(String name) {
      Preconditions.checkState(!isDefinedLocally(name));
      locals.put(name, JSType.TOP_OBJECT);
      localNamespaces.add(name);
    }

    void updateTypeOfLocal(String name, JSType newDeclType) {
      locals.put(name, newDeclType);
    }

    void addOuterVar(String name) {
      outerVars.add(name);
    }

    void addClassType(String name, NominalType klass) {
      localClassDefs.put(name, klass);
    }

    void finalizeScope() {
      Iterator<String> it = localFunDefs.keySet().iterator();
      while (it.hasNext()) {
        String name = it.next();
        if (!TypeUtils.isIdentifier(name)) {
          it.remove();
        }
      }
      localNamespaces = null;
      localClassDefs = null;
    }
  }
}