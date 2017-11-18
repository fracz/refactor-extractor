/*
 * Copyright (C) 2015 Google, Inc.
 *
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
package com.google.auto.value.processor;

import com.google.auto.common.MoreTypes;
import com.google.common.base.Equivalence;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.beans.Introspector;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Classifies methods inside builder types, based on their names and parameter and return types.
 *
 * @author Éamonn McManus
 */
class BuilderMethodClassifier {
  private static final Equivalence<TypeMirror> TYPE_EQUIVALENCE = MoreTypes.equivalence();

  private final ErrorReporter errorReporter;
  private final TypeElement autoValueClass;
  private final TypeElement builderType;
  private final ImmutableBiMap<ExecutableElement, String> getterToPropertyName;
  private final Set<ExecutableElement> buildMethods = Sets.newLinkedHashSet();
  private final Map<String, ExecutableElement> propertyNameToPrefixedSetter =
      Maps.newLinkedHashMap();
  private final Map<String, ExecutableElement> propertyNameToUnprefixedSetter =
      Maps.newLinkedHashMap();
  private boolean settersPrefixed;

  private BuilderMethodClassifier(
      ErrorReporter errorReporter,
      TypeElement autoValueClass,
      TypeElement builderType,
      ImmutableBiMap<ExecutableElement, String> getterToPropertyName) {
    this.errorReporter = errorReporter;
    this.autoValueClass = autoValueClass;
    this.builderType = builderType;
    this.getterToPropertyName = getterToPropertyName;
  }

  /**
   * Classify the given methods from a builder type and its ancestors.
   *
   * @param methods the methods in {@code builderType} and its ancestors.
   * @param errorReporter where to report errors.
   * @param autoValueClass the {@code AutoValue} class containing the builder.
   * @param builderType the builder class or interface within {@code autoValueClass}.
   * @param getterToPropertyName a map from getter methods to the properties they get.
   *
   * @return an {@code Optional} that contains the results of the classification if it was
   *     successful or nothing if it was not.
   */
  static Optional<BuilderMethodClassifier> classify(
      Iterable<ExecutableElement> methods,
      ErrorReporter errorReporter,
      TypeElement autoValueClass,
      TypeElement builderType,
      ImmutableBiMap<ExecutableElement, String> getterToPropertyName) {
    BuilderMethodClassifier classifier = new BuilderMethodClassifier(
        errorReporter, autoValueClass, builderType, getterToPropertyName);
    if (classifier.classifyMethods(methods)) {
      return Optional.of(classifier);
    } else {
      return Optional.absent();
    }
  }

  /**
   * Returns a map from the name of a property to the method that sets it. If the property is
   * defined by an abstract method in the {@code @AutoValue} class called {@code foo()} or
   * {@code getFoo()} then the name of the property is {@code foo} and there will be an entry in
   * the map where the key is {@code "foo"} and the value is a method in the builder called
   * {@code foo} or {@code setFoo}.
   */
  Map<String, ExecutableElement> propertyNameToSetter() {
    return settersPrefixed ? propertyNameToPrefixedSetter : propertyNameToUnprefixedSetter;
  }

  /**
   * Returns the methods that were identified as {@code build()} methods. These are methods that
   * have no parameters and return the {@code @AutoValue} type, conventionally called
   * {@code build()}.
   */
  Set<ExecutableElement> buildMethods() {
    return ImmutableSet.copyOf(buildMethods);
  }

  /**
   * Classifies the given methods and sets the state of this object based on what is found.
   */
  private boolean classifyMethods(Iterable<ExecutableElement> methods) {
    boolean ok = true;
    for (ExecutableElement method : methods) {
      ok &= classifyMethod(method);
    }
    if (!ok) {
      return false;
    }
    Map<String, ExecutableElement> propertyNameToSetter;
    if (propertyNameToPrefixedSetter.isEmpty()) {
      propertyNameToSetter = propertyNameToUnprefixedSetter;
      this.settersPrefixed = false;
    } else if (propertyNameToUnprefixedSetter.isEmpty()) {
      propertyNameToSetter = propertyNameToPrefixedSetter;
      this.settersPrefixed = true;
    } else {
      errorReporter.reportError(
          "If any setter methods use the setFoo convention then all must",
          propertyNameToUnprefixedSetter.values().iterator().next());
      return false;
    }
    for (Map.Entry<ExecutableElement, String> getterEntry : getterToPropertyName.entrySet()) {
      String property = getterEntry.getValue();
      if (!propertyNameToSetter.containsKey(property)) {
        String setterName = settersPrefixed ? prefixWithSet(property) : property;
        String error = String.format("Expected a method with this signature: %s%s %s(%s)",
            builderType,
            typeParamsString(),
            setterName,
            getterEntry.getKey().getReturnType());
        errorReporter.reportError(error, builderType);
        ok = false;
      }
    }
    return ok;
  }

  /**
   * Classify a method and update the state of this object based on what is found.
   *
   * @return true if the method was successfully classified, false if an error has been reported.
   */
  private boolean classifyMethod(ExecutableElement method) {
    switch (method.getParameters().size()) {
      case 0:
        return classifyMethodNoArgs(method);
      case 1:
        return classifyMethodOneArg(method);
      default:
        errorReporter.reportError("Builder methods must have 0 or 1 parameters", method);
        return false;
    }
  }

  /**
   * Classify a method given that it has no arguments. Currently a method with no
   * arguments can only be a {@code build()} method, meaning that its return type must be the
   * {@code @AutoValue} class.
   *
   * @return true if the method was successfully classified, false if an error has been reported.
   */
  private boolean classifyMethodNoArgs(ExecutableElement method) {
    TypeMirror returnType = method.getReturnType();

    if (TYPE_EQUIVALENCE.equivalent(returnType, autoValueClass.asType())) {
      buildMethods.add(method);
      return true;
    }

    errorReporter.reportError(
        "Method without arguments should be a build method returning "
        + autoValueClass + typeParamsString(), method);
    return false;
  }

  /**
   * Classify a method given that it has one argument. Currently, a method with one argument can
   * only be a setter, meaning that it must look like {@code foo(T)} or {@code setFoo(T)}, where
   * the {@code AutoValue} class has a property called {@code foo} of type {@code T}.
   *
   * @return true if the method was successfully classified, false if an error has been reported.
   */
  private boolean classifyMethodOneArg(ExecutableElement method) {
    String methodName = method.getSimpleName().toString();
    Map<String, ExecutableElement> propertyNameToGetter = getterToPropertyName.inverse();
    String propertyName = null;
    ExecutableElement valueGetter = propertyNameToGetter.get(methodName);
    Map<String, ExecutableElement> propertyNameToSetter = null;
    if (valueGetter != null) {
      propertyName = methodName;
      propertyNameToSetter = propertyNameToUnprefixedSetter;
    } else if (valueGetter == null && methodName.startsWith("set") && methodName.length() > 3) {
      propertyName = Introspector.decapitalize(methodName.substring(3));
      propertyNameToSetter = propertyNameToPrefixedSetter;
      valueGetter = propertyNameToGetter.get(propertyName);
    }
    if (valueGetter == null) {
      errorReporter.reportError(
          "Method does not correspond to a property of " + autoValueClass, method);
      return false;
    }
    TypeMirror parameterType = method.getParameters().get(0).asType();
    TypeMirror expectedType = valueGetter.getReturnType();
    if (!TYPE_EQUIVALENCE.equivalent(parameterType, expectedType)) {
      errorReporter.reportError(
          "Parameter type of setter method should be " + expectedType + " to match getter "
          + autoValueClass + "." + valueGetter.getSimpleName(), method);
      return false;
    } else if (!TYPE_EQUIVALENCE.equivalent(method.getReturnType(), builderType.asType())) {
      errorReporter.reportError(
          "Setter methods must return " + builderType + typeParamsString(), method);
      return false;
    } else {
      propertyNameToSetter.put(propertyName, method);
      return true;
    }
  }

  private String prefixWithSet(String propertyName) {
    // This is not internationalizationally correct, but it corresponds to what
    // Introspector.decapitalize does.
    return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
  }

  private String typeParamsString() {
    return TypeSimplifier.actualTypeParametersString(autoValueClass);
  }
}