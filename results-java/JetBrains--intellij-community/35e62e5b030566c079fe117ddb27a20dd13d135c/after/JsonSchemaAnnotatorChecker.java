/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package com.jetbrains.jsonSchema.impl;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ThreeState;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.jsonSchema.JsonSchemaFileType;
import com.jetbrains.jsonSchema.ide.JsonSchemaService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Irina.Chernushina on 4/25/2017.
 */
class JsonSchemaAnnotatorChecker {
  private final Map<PsiElement, String> myErrors;
  private boolean myHadTypeError;

  private JsonSchemaAnnotatorChecker() {
    myErrors = new HashMap<>();
  }

  public Map<PsiElement, String> getErrors() {
    return myErrors;
  }

  public boolean isHadTypeError() {
    return myHadTypeError;
  }

  public static JsonSchemaAnnotatorChecker checkByMatchResult(@NotNull JsonValueAdapter elementToCheck,
                                                              @NotNull final MatchResult result) {
    final List<JsonSchemaAnnotatorChecker> checkers = new ArrayList<>();
    if (result.myExcludingSchemas.isEmpty() && result.mySchemas.size() == 1) {
      final JsonSchemaAnnotatorChecker checker = new JsonSchemaAnnotatorChecker();
      checker.checkByScheme(elementToCheck, result.mySchemas.iterator().next());
      checkers.add(checker);
    }
    else {
      if (!result.mySchemas.isEmpty()) {
        checkers.add(processSchemasVariants(result.mySchemas, elementToCheck, false));
      }
      if (!result.myExcludingSchemas.isEmpty()) {
        checkers.add(processSchemasVariants(result.myExcludingSchemas, elementToCheck, true));
      }
    }
    if (checkers.isEmpty()) return null;

    return checkers.stream()
      .filter(checker -> !checker.isHadTypeError())
      .findFirst()
      .orElse(checkers.get(0));
  }

  private void error(final String error, final PsiElement holder) {
    if (myErrors.containsKey(holder)) return;
    myErrors.put(holder, error);
  }

  private void typeError(final @NotNull PsiElement value) {
    error("Type is not allowed", value);
    myHadTypeError = true;
  }

  private void checkByScheme(@NotNull JsonValueAdapter value, @NotNull JsonSchemaObject schema) {
    final JsonSchemaType type = JsonSchemaType.getType(value);
    if (type != null) {
      JsonSchemaType schemaType = getMatchingSchemaType(schema, type);
      if (schemaType == null && schema.hasSpecifiedType()) {
        typeError(value.getDelegate());
      }
      else if (JsonSchemaType._boolean.equals(type)) {
        checkForEnum(value.getDelegate(), schema);
      }
      else if (JsonSchemaType._number.equals(type) || JsonSchemaType._integer.equals(type)) {
        checkNumber(value.getDelegate(), schema, schemaType);
        checkForEnum(value.getDelegate(), schema);
      }
      else if (JsonSchemaType._string.equals(type)) {
        checkString(value.getDelegate(), schema);
        checkForEnum(value.getDelegate(), schema);
      }
      else if (JsonSchemaType._array.equals(type)) {
        checkArray(value, schema);
        checkForEnum(value.getDelegate(), schema);
      }
      else if (JsonSchemaType._object.equals(type)) {
        checkObject(value, schema);
        checkForEnum(value.getDelegate(), schema);
      }
    }

    if (schema.getNot() != null) {
      final JsonSchemaAnnotatorChecker checker = checkByMatchResult(value, new JsonSchemaResolver(schema.getNot()).detailedResolve());
      if (checker == null || checker.isCorrect()) error("Validates against 'not' schema", value.getDelegate());
    }
  }

  private void checkObjectBySchemaRecordErrors(@NotNull JsonSchemaObject schema, @NotNull JsonValueAdapter object) {
    final JsonSchemaAnnotatorChecker checker = checkByMatchResult(object, new JsonSchemaResolver(schema).detailedResolve());
    if (checker != null) {
      myHadTypeError = checker.isHadTypeError();
      myErrors.putAll(checker.getErrors());
    }
  }

  private void checkObject(@NotNull JsonValueAdapter value, @NotNull JsonSchemaObject schema) {
    final JsonObjectValueAdapter object = value.getAsObject();
    if (object == null) return;

    //noinspection ConstantConditions
    final List<JsonPropertyAdapter> propertyList = object.getPropertyList();
    final Set<String> set = new HashSet<>();
    for (JsonPropertyAdapter property : propertyList) {
      final String name = StringUtil.notNullize(property.getName());

      final JsonSchemaVariantsTreeBuilder.Step step = JsonSchemaVariantsTreeBuilder.Step.createPropertyStep(name);
      final Pair<ThreeState, JsonSchemaObject> pair = step.step(schema, true);
      if (ThreeState.NO.equals(pair.getFirst()) && !set.contains(name)) {
        error("Property '" + name + "' is not allowed", property.getDelegate());
      }
      else if (ThreeState.UNSURE.equals(pair.getFirst()) && property.getValue() != null) {
        checkObjectBySchemaRecordErrors(pair.getSecond(), property.getValue());
      }
      set.add(name);
    }

    if (object.shouldCheckIntegralRequirements()) {
      final List<String> required = schema.getRequired();
      if (required != null) {
        for (String req : required) {
          if (!set.contains(req)) {
            error("Missing required property '" + req + "'", value.getDelegate());
          }
        }
      }
      if (schema.getMinProperties() != null && propertyList.size() < schema.getMinProperties()) {
        error("Number of properties is less than " + schema.getMinProperties(), value.getDelegate());
      }
      if (schema.getMaxProperties() != null && propertyList.size() > schema.getMaxProperties()) {
        error("Number of properties is greater than " + schema.getMaxProperties(), value.getDelegate());
      }
      final Map<String, List<String>> dependencies = schema.getPropertyDependencies();
      if (dependencies != null) {
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
          if (set.contains(entry.getKey())) {
            final List<String> list = entry.getValue();
            for (String s : list) {
              if (!set.contains(s)) {
                error("Dependency is violated: '" + s + "' must be specified, since '" + entry.getKey() + "' is specified",
                      value.getDelegate());
              }
            }
          }
        }
      }
      final Map<String, JsonSchemaObject> schemaDependencies = schema.getSchemaDependencies();
      if (schemaDependencies != null) {
        for (Map.Entry<String, JsonSchemaObject> entry : schemaDependencies.entrySet()) {
          if (set.contains(entry.getKey())) {
            checkObjectBySchemaRecordErrors(entry.getValue(), value);
          }
        }
      }
    }

    validateAsJsonSchema(object.getDelegate());
  }

  private void validateAsJsonSchema(@NotNull PsiElement objElement) {
    final JsonObject object = ObjectUtils.tryCast(objElement, JsonObject.class);
    if (object == null) return;
    if (JsonSchemaFileType.INSTANCE.equals(object.getContainingFile().getFileType())) {

      final VirtualFile schemaFile = object.getContainingFile().getVirtualFile();
      if (schemaFile == null) return;

      final JsonSchemaObject schemaObject = JsonSchemaService.Impl.get(object.getProject()).getSchemaObjectForSchemaFile(schemaFile);
      if (schemaObject == null) return;

      final List<JsonSchemaVariantsTreeBuilder.Step> steps =
        skipProperties(JsonOriginalPsiWalker.INSTANCE.findPosition(object, false, true));
      // !! not root schema, because we validate the schema written in the file itself
      final MatchResult result = new JsonSchemaResolver(schemaObject, false, steps).detailedResolve();
      final List<JsonSchemaObject> schemas = new ArrayList<>(result.mySchemas);
      schemas.addAll(result.myExcludingSchemas);
      schemas.forEach(schema -> {
        if (schemaFile.equals(schema.getSchemaFile())) {
          final Map<SmartPsiElementPointer<JsonObject>, String> invalidPatternProperties = schema.getInvalidPatternProperties();
          if (invalidPatternProperties != null) {
            for (Map.Entry<SmartPsiElementPointer<JsonObject>, String> entry : invalidPatternProperties.entrySet()) {
              final JsonObject element = entry.getKey().getElement();
              if (element == null || !element.isValid()) continue;
              final PsiElement parent = element.getParent();
              if (parent instanceof JsonProperty) {
                error(StringUtil.convertLineSeparators(entry.getValue()), ((JsonProperty)parent).getNameElement());
              }
            }
          }
          schema.getProperties().values().forEach(prop -> {
            final String patternError = prop.getPatternError();
            if (patternError != null && prop.getPattern() != null) {
              final SmartPsiElementPointer<JsonObject> pointer = prop.getPeerPointer();
              final JsonObject element = pointer.getElement();
              if (element != null && element.isValid()) {
                final JsonProperty pattern = element.findProperty("pattern");
                if (pattern != null) {
                  error(StringUtil.convertLineSeparators(patternError), pattern.getValue());
                }
              }
            }
          });
        }
      });
    }
  }

  private static List<JsonSchemaVariantsTreeBuilder.Step> skipProperties(List<JsonSchemaVariantsTreeBuilder.Step> position) {
    final Iterator<JsonSchemaVariantsTreeBuilder.Step> iterator = position.iterator();
    boolean canSkip = true;
    while (iterator.hasNext()) {
      final JsonSchemaVariantsTreeBuilder.Step step = iterator.next();
      if (canSkip && step.isFromObject() && JsonSchemaObject.PROPERTIES.equals(step.getName())) {
        iterator.remove();
        canSkip = false;
      }
      else {
        canSkip = true;
      }
    }
    return position;
  }

  private void checkForEnum(PsiElement value, JsonSchemaObject schema) {
    //enum values + pattern -> don't check enum values
    if (schema.getEnum() == null || schema.getPattern() != null) return;
    final String text = StringUtil.notNullize(value.getText());
    final List<Object> objects = schema.getEnum();
    for (Object object : objects) {
      if (JsonLikePsiWalker.getWalker(value, schema).onlyDoubleQuotesForStringLiterals()) {
        if (object.toString().equalsIgnoreCase(text)) return;
      }
      else {
        if (equalsIgnoreQuotesAndCase(object.toString(), text)) return;
      }
    }
    error("Value should be one of: [" + StringUtil.join(objects, o -> o.toString(), ", ") + "]", value);
  }

  private static boolean equalsIgnoreQuotesAndCase(@NotNull final String s1, @NotNull final String s2) {
    final boolean quoted1 = StringUtil.isQuotedString(s1);
    final boolean quoted2 = StringUtil.isQuotedString(s2);
    if (quoted1 != quoted2) return false;
    if (!quoted1) return s1.equalsIgnoreCase(s2);
    return StringUtil.unquoteString(s1).equalsIgnoreCase(StringUtil.unquoteString(s2));
  }

  private void checkArray(JsonValueAdapter value, JsonSchemaObject schema) {
    final JsonArrayValueAdapter asArray = value.getAsArray();
    if (asArray == null) return;
    final List<JsonValueAdapter> elements = asArray.getElements();
    if (schema.getMinLength() != null && elements.size() < schema.getMinLength()) {
      error("Array is shorter than " + schema.getMinLength(), value.getDelegate());
      return;
    }
    checkArrayItems(value, elements, schema);
  }

  @NotNull
  private static JsonSchemaAnnotatorChecker processSchemasVariants(@NotNull final Collection<JsonSchemaObject> collection,
                                                                   @NotNull final JsonValueAdapter value, boolean isOneOf) {
    final JsonSchemaAnnotatorChecker checker = new JsonSchemaAnnotatorChecker();
    final JsonSchemaType type = JsonSchemaType.getType(value);
    if (type == null) {
      checker.typeError(value.getDelegate());
    }
    else {
      final List<JsonSchemaObject> filtered = collection.stream()
        .filter(schema -> areSchemaTypesCompatible(schema, type))
        .collect(Collectors.toList());
      if (filtered.isEmpty()) checker.typeError(value.getDelegate());
      else {
        if (isOneOf) {
          checker.processOneOf(value, filtered);
        }
        else {
          checker.processAnyOf(value, filtered);
        }
      }
    }
    return checker;
  }

  private static boolean areSchemaTypesCompatible(@NotNull final JsonSchemaObject schema, @NotNull final JsonSchemaType type) {
    if (getMatchingSchemaType(schema, type) != null) return true;
    if (schema.getEnum() != null && (JsonSchemaType._integer.equals(type) ||
                                     JsonSchemaType._number.equals(type) || JsonSchemaType._boolean.equals(type) ||
                                     JsonSchemaType._string.equals(type))) {
      return true;
    }
    return false;
  }

  @Nullable
  private static JsonSchemaType getMatchingSchemaType(@NotNull JsonSchemaObject schema, @NotNull JsonSchemaType input) {
    if (schema.getType() != null) {
      JsonSchemaType matchType = schema.getType();
      if (matchType == input) {
        return matchType;
      }
      if (input == JsonSchemaType._integer && matchType == JsonSchemaType._number) {
        return JsonSchemaType._number;
      }
    }
    if (schema.getTypeVariants() != null) {
      List<JsonSchemaType> matchTypes = schema.getTypeVariants();
      if (matchTypes.contains(input)) {
        return input;
      }
      if (input == JsonSchemaType._integer && matchTypes.contains(JsonSchemaType._number)) {
        return JsonSchemaType._number;
      }
    }
    if (!schema.getProperties().isEmpty() && JsonSchemaType._object.equals(input)) return JsonSchemaType._object;
    return null;
  }

  private void checkArrayItems(@NotNull JsonValueAdapter array, @NotNull final List<JsonValueAdapter> list, final JsonSchemaObject schema) {
    if (schema.isUniqueItems()) {
      final MultiMap<String, JsonValueAdapter> valueTexts = new MultiMap<>();
      list.forEach(item -> valueTexts.putValue(item.getDelegate().getText(), item));

      valueTexts.keySet().stream().filter(key -> valueTexts.get(key).size() > 1)
        .map(key -> valueTexts.get(key))
        .flatMap(Collection::stream)
        .forEach(item -> error("Item is not unique", item.getDelegate()));
    }
    if (schema.getItemsSchema() != null) {
      list.forEach(item -> checkObjectBySchemaRecordErrors(schema.getItemsSchema(), item));
    }
    else if (schema.getItemsSchemaList() != null) {
      final Iterator<JsonSchemaObject> iterator = schema.getItemsSchemaList().iterator();
      for (JsonValueAdapter arrayValue : list) {
        if (iterator.hasNext()) {
          checkObjectBySchemaRecordErrors(iterator.next(), arrayValue);
        }
        else {
          if (!Boolean.TRUE.equals(schema.getAdditionalItemsAllowed())) {
            error("Additional items are not allowed", arrayValue.getDelegate());
          }
        }
      }
    }
    if (schema.getMinItems() != null && list.size() < schema.getMinItems()) {
      error("Array is shorter than " + schema.getMinItems(), array.getDelegate());
    }
    if (schema.getMaxItems() != null && list.size() > schema.getMaxItems()) {
      error("Array is longer than " + schema.getMaxItems(), array.getDelegate());
    }
  }

  private void checkString(PsiElement propValue, JsonSchemaObject schema) {
    final String value = StringUtil.unquoteString(propValue.getText());
    if (schema.getMinLength() != null) {
      if (value.length() < schema.getMinLength()) {
        error("String is shorter than " + schema.getMinLength(), propValue);
        return;
      }
    }
    if (schema.getMaxLength() != null) {
      if (value.length() > schema.getMaxLength()) {
        error("String is longer than " + schema.getMaxLength(), propValue);
        return;
      }
    }
    if (schema.getPattern() != null) {
      if (schema.getPatternError() != null) {
        error("Can not check string by pattern because of error: " + StringUtil.convertLineSeparators(schema.getPatternError()),
              propValue);
      }
      if (!schema.checkByPattern(value)) {
        error("String is violating the pattern: '" + StringUtil.convertLineSeparators(schema.getPattern()) + "'", propValue);
      }
    }
    // I think we are not gonna to support format, there are a couple of RFCs there to check upon..
    /*
    if (schema.getFormat() != null) {
      LOG.info("Unsupported property used: 'format'");
    }*/
  }

  private void checkNumber(PsiElement propValue, JsonSchemaObject schema, JsonSchemaType schemaType) {
    Number value;
    if (JsonSchemaType._integer.equals(schemaType)) {
      try {
        value = Integer.valueOf(propValue.getText());
      }
      catch (NumberFormatException e) {
        error("Integer value expected", propValue);
        return;
      }
    }
    else {
      try {
        value = Double.valueOf(propValue.getText());
      }
      catch (NumberFormatException e) {
        error("Double value expected", propValue);
        return;
      }
    }
    final Number multipleOf = schema.getMultipleOf();
    if (multipleOf != null) {
      final double leftOver = value.doubleValue() % multipleOf.doubleValue();
      if (leftOver > 0.000001) {
        final String multipleOfValue = String.valueOf(Math.abs(multipleOf.doubleValue() - multipleOf.intValue()) < 0.000001 ?
                                                      multipleOf.intValue() : multipleOf);
        error("Is not multiple of " + multipleOfValue, propValue);
        return;
      }
    }
    if (schema.getMinimum() != null) {
      checkMinimum(schema, value, propValue, schemaType);
    }
    if (schema.getMaximum() != null) {
      checkMaximum(schema, value, propValue, schemaType);
    }
  }

  private void checkMaximum(JsonSchemaObject schema, Number value, PsiElement propertyValue,
                            @NotNull JsonSchemaType propValueType) {
    if (JsonSchemaType._integer.equals(propValueType)) {
      final int intValue = schema.getMaximum().intValue();
      if (Boolean.TRUE.equals(schema.isExclusiveMaximum())) {
        if (value.intValue() >= intValue) {
          error("Greater than an exclusive maximum " + intValue, propertyValue);
        }
      }
      else {
        if (value.intValue() > intValue) {
          error("Greater than a maximum " + intValue, propertyValue);
        }
      }
    }
    else {
      final double doubleValue = schema.getMaximum().doubleValue();
      if (Boolean.TRUE.equals(schema.isExclusiveMaximum())) {
        if (value.doubleValue() >= doubleValue) {
          error("Greater than an exclusive maximum " + schema.getMinimum(), propertyValue);
        }
      }
      else {
        if (value.doubleValue() > doubleValue) {
          error("Greater than a maximum " + schema.getMaximum(), propertyValue);
        }
      }
    }
  }

  private void checkMinimum(JsonSchemaObject schema, Number value, PsiElement propertyValue,
                            @NotNull JsonSchemaType schemaType) {
    if (JsonSchemaType._integer.equals(schemaType)) {
      final int intValue = schema.getMinimum().intValue();
      if (Boolean.TRUE.equals(schema.isExclusiveMinimum())) {
        if (value.intValue() <= intValue) {
          error("Less than an exclusive minimum " + intValue, propertyValue);
        }
      }
      else {
        if (value.intValue() < intValue) {
          error("Less than a minimum " + intValue, propertyValue);
        }
      }
    }
    else {
      final double doubleValue = schema.getMinimum().doubleValue();
      if (Boolean.TRUE.equals(schema.isExclusiveMinimum())) {
        if (value.doubleValue() <= doubleValue) {
          error("Less than an exclusive minimum " + schema.getMinimum(), propertyValue);
        }
      }
      else {
        if (value.doubleValue() < doubleValue) {
          error("Less than a minimum " + schema.getMinimum(), propertyValue);
        }
      }
    }
  }

  private void processOneOf(@NotNull JsonValueAdapter value, List<JsonSchemaObject> oneOf) {
    final Map<PsiElement, String> errors = new HashMap<>();
    int cntCorrect = 0;
    for (JsonSchemaObject object : oneOf) {
      // skip it if something JS awaited, we do not process it currently
      if (object.isShouldValidateAgainstJSType()) continue;

      final JsonSchemaAnnotatorChecker checker = new JsonSchemaAnnotatorChecker();
      checker.checkByScheme(value, object);
      if (checker.isCorrect()) {
        errors.clear();
        ++cntCorrect;
      }
      else {
        if (errors.isEmpty() || notTypeError(value.getDelegate(), checker)) {
          errors.clear();
          errors.putAll(checker.getErrors());
        }
      }
    }
    if (cntCorrect == 1) return;
    if (cntCorrect > 0) {
      final JsonSchemaType type = JsonSchemaType.getType(value);
      if (type != null) error("Validates to more than one variant", value.getDelegate());
    }
    else {
      if (!errors.isEmpty()) {
        for (Map.Entry<PsiElement, String> entry : errors.entrySet()) {
          error(entry.getValue(), entry.getKey());
        }
      }
    }
  }

  private static boolean notTypeError(PsiElement value, JsonSchemaAnnotatorChecker checker) {
    if (!checker.isHadTypeError()) return true;
    return !checker.getErrors().containsKey(value);
  }

  private void processAnyOf(@NotNull JsonValueAdapter value, List<JsonSchemaObject> anyOf) {
    final Map<PsiElement, String> errors = new HashMap<>();
    for (JsonSchemaObject object : anyOf) {
      final JsonSchemaAnnotatorChecker checker = new JsonSchemaAnnotatorChecker();
      checker.checkByScheme(value, object);
      if (checker.isCorrect()) {
        return;
      }
      // maybe we still find the correct schema - continue to iterate
      if (errors.isEmpty() && notTypeError(value.getDelegate(), checker)) {
        errors.clear();
        errors.putAll(checker.getErrors());
      }
    }
    if (!errors.isEmpty()) {
      for (Map.Entry<PsiElement, String> entry : errors.entrySet()) {
        error(entry.getValue(), entry.getKey());
      }
    }
  }

  public boolean isCorrect() {
    return myErrors.isEmpty();
  }
}