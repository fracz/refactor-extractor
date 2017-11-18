/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import com.intellij.json.psi.*;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.jetbrains.jsonSchema.JsonSchemaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Irina.Chernushina on 8/31/2015.
 */
class JsonBySchemaObjectAnnotator implements Annotator {
  private final static Logger LOG = Logger.getInstance("#com.jetbrains.jsonSchema.JsonBySchemaAnnotator");
  private static final Key<Set<PsiElement>> ANNOTATED_PROPERTIES = Key.create("JsonSchema.Properties.Annotated");
  @NotNull private final VirtualFile mySchemaFile;
  private final JsonSchemaObject myRootSchema;

  public JsonBySchemaObjectAnnotator(@NotNull VirtualFile schemaFile, @NotNull JsonSchemaObject schema) {
    mySchemaFile = schemaFile;
    myRootSchema = schema;
  }

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    final PsiFile psiFile = element.getContainingFile();
    if (! (psiFile instanceof JsonFile)) return;

    final JsonProperty firstProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, false);
    if (firstProp == null) {
      final JsonValue root = findTopLevelElement(element);
      if (root != null) {
        checkRootObject(holder, root);
      }
      return;
    }
    if (checkIfAlreadyProcessed(holder, firstProp)) return;

    final List<BySchemaChecker> checkers = new ArrayList<>();
    JsonSchemaWalker.findSchemasForAnnotation(firstProp, new JsonSchemaWalker.CompletionSchemesConsumer() {
      @Override
      public void consume(boolean isName,
                          @NotNull JsonSchemaObject schema,
                          @NotNull VirtualFile schemaFile,
                          @NotNull List<JsonSchemaWalker.Step> steps) {
        final BySchemaChecker checker = new BySchemaChecker();
        final Set<String> validatedProperties = new HashSet<>();
        checker.checkByScheme(firstProp.getValue(), schema, validatedProperties);
        checkers.add(checker);
      }
    }, myRootSchema, mySchemaFile);

    if (checkers.isEmpty()) return;

    BySchemaChecker checker = null;
    if (checkers.size() == 1) {
      checker = checkers.get(0);
    } else {
      for (BySchemaChecker schemaChecker : checkers) {
        if (!schemaChecker.isHadTypeError()) {
          checker = schemaChecker;
          break;
        }
      }
      if (checker == null) {
        checker = checkers.get(0);
      }
    }

    if (processCheckerResults(holder, checker)) return;
    if (firstProp.getParent() instanceof JsonObject && firstProp.getParent().getParent() instanceof PsiFile) {
      checkRootObject(holder, firstProp.getParent());
    }
  }

  private static JsonValue findTopLevelElement(@NotNull PsiElement element) {
    PsiElement current = element;
    while (true) {
      final JsonValue value = PsiTreeUtil.getParentOfType(current, JsonValue.class, true);
      if (value != null && (value.getParent() instanceof PsiFile)) return value;
      if (value == null) return current instanceof JsonValue ? (JsonValue)current : null;
      current = value;
    }
  }

  private void checkRootObject(@NotNull AnnotationHolder holder, PsiElement property) {
    JsonValue object = property instanceof JsonObject ? (JsonObject)property : PsiTreeUtil.getParentOfType(property, JsonObject.class);
    if (object == null) object = property instanceof JsonArray ? (JsonValue)property : PsiTreeUtil.getParentOfType(property, JsonArray.class);
    if (object != null) {
      final BySchemaChecker rootChecker = new BySchemaChecker();

      final Set<String> validatedProperties = new HashSet<>();
      rootChecker.checkByScheme(object, myRootSchema, validatedProperties);
      processCheckerResults(holder, rootChecker);
    }
  }

  private static boolean checkIfAlreadyProcessed(@NotNull AnnotationHolder holder, PsiElement property) {
    final AnnotationSession session = holder.getCurrentAnnotationSession();
    Set<PsiElement> data = session.getUserData(ANNOTATED_PROPERTIES);
    if (data == null) {
      data = new HashSet<>();
      session.putUserData(ANNOTATED_PROPERTIES, data);
    }
    if (data.contains(property)) return true;
    data.add(property);
    return false;
  }

  private static boolean processCheckerResults(@NotNull AnnotationHolder holder, BySchemaChecker checker) {
    if (! checker.isCorrect()) {
      for (Map.Entry<PsiElement, String> entry : checker.getErrors().entrySet()) {
        if (checkIfAlreadyProcessed(holder, entry.getKey())) continue;
        holder.createWarningAnnotation(entry.getKey(), entry.getValue());
      }
      return true;
    }
    return false;
  }

  private static class BySchemaChecker {
    private final Map<PsiElement, String> myErrors;
    private boolean myHadTypeError;

    public BySchemaChecker() {
      myErrors = new HashMap<>();
    }

    public Map<PsiElement, String> getErrors() {
      return myErrors;
    }

    public boolean isHadTypeError() {
      return myHadTypeError;
    }

    private void error(final String error, final PsiElement holder) {
      if (myErrors.containsKey(holder)) return;
      myErrors.put(holder, error);
    }

    private void typeError(final @NotNull JsonValue value) {
      error("Type is not allowed", value);
      myHadTypeError = true;
    }

    private void checkByScheme(JsonValue value, JsonSchemaObject schema, final Set<String> validatedProperties) {
      if (value == null) return;

      if (schema.getAnyOf() != null && ! schema.getAnyOf().isEmpty()) {
        processAnyOf(value, schema, validatedProperties);
      }
      if (schema.getOneOf() != null && ! schema.getOneOf().isEmpty()) {
        processOneOf(value, schema, validatedProperties);
      }
      if (schema.getAllOf() != null && ! schema.getAllOf().isEmpty()) {
        processAllOf(value, schema, validatedProperties);
      }

      final JsonSchemaType type = getType(value);
      if (type == null) {
        typeError(value);
      } else {
        JsonSchemaType schemaType = matchSchemaType(schema, type);
        if (schemaType == null && schema.hasSpecifiedType()) {
          typeError(value);
        }
        else if (JsonSchemaType._boolean.equals(type)) {
          checkForEnum(value, schema);
        }
        else if (JsonSchemaType._number.equals(type) || JsonSchemaType._integer.equals(type)) {
          checkNumber(value, schema, schemaType);
          checkForEnum(value, schema);
        }
        else if (JsonSchemaType._string.equals(type)) {
          checkString(value, schema);
          checkForEnum(value, schema);
        }
        else if (JsonSchemaType._array.equals(type)) {
          checkArray(value, schema);
          checkForEnum(value, schema);
        }
        else if (JsonSchemaType._object.equals(type)) {
          checkObject(value, schema, validatedProperties);
          checkForEnum(value, schema);
        }
      }

      if (schema.getNot() != null) {
        final BySchemaChecker checker = new BySchemaChecker();
        checker.checkByScheme(value, schema.getNot(), new HashSet<>());
        if (checker.isCorrect()) error("Validates against 'not' schema", value);
      }
    }

    private void checkObject(JsonValue value, JsonSchemaObject schema, Set<String> validatedProperties) {
      final Map<String, JsonSchemaObject> properties = schema.getProperties();
      final JsonObject object = ObjectUtils.tryCast(value, JsonObject.class);
      //noinspection ConstantConditions
      final List<JsonProperty> propertyList = object.getPropertyList();
      final Map<String, JsonProperty> map = new HashMap<>();
      for (JsonProperty property : propertyList) {
        map.put(property.getName(), property);
        final JsonSchemaObject propertySchema = properties.get(property.getName());
        if (propertySchema != null) {
          checkByScheme(property.getValue(), propertySchema, new HashSet<>());
        }
        else {
          final JsonSchemaObject patternSchema = schema.getMatchingPatternPropertySchema(property.getName());
          if (patternSchema != null) {
            checkByScheme(property.getValue(), patternSchema, new HashSet<>());
          } else if (schema.getAdditionalPropertiesSchema() != null) {
            checkByScheme(property.getValue(), schema.getAdditionalPropertiesSchema(), new HashSet<>());
          }
          else if (!Boolean.TRUE.equals(schema.getAdditionalPropertiesAllowed()) && !validatedProperties.contains(property.getName())) {
            error("Property '" + property.getName() + "' is not allowed", property);
          }
        }
        validatedProperties.add(property.getName());
      }

      final List<String> required = schema.getRequired();
      if (required != null) {
        for (String req : required) {
          if (!map.containsKey(req)) {
            error("Missing required property '" + req +"'", value);
          }
        }
      }
      if (schema.getMinProperties() != null && propertyList.size() < schema.getMinProperties()) {
        error("Number of properties is less than " + schema.getMinProperties(), value);
      }
      if (schema.getMaxProperties() != null && propertyList.size() > schema.getMaxProperties()) {
        error("Number of properties is greater than " + schema.getMaxProperties(), value);
      }
      final Map<String, List<String>> dependencies = schema.getPropertyDependencies();
      if (dependencies != null) {
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
          if (map.containsKey(entry.getKey())) {
            final List<String> list = entry.getValue();
            for (String s : list) {
              if (!map.containsKey(s)) {
                error("Dependency is violated: '" + s + "' must be specified, since '" + entry.getKey() + "' is specified", value);
              }
            }
          }
        }
      }
      final Map<String, JsonSchemaObject> schemaDependencies = schema.getSchemaDependencies();
      if (schemaDependencies != null) {
        for (Map.Entry<String, JsonSchemaObject> entry : schemaDependencies.entrySet()) {
          if (map.containsKey(entry.getKey())) {
            checkByScheme(value, entry.getValue(), new HashSet<>());
          }
        }
      }

      validateAsJsonSchema(object);
    }

    private void validateAsJsonSchema(@NotNull JsonObject object) {
      if (JsonSchemaFileType.INSTANCE.equals(object.getContainingFile().getFileType())) {

        final VirtualFile schemaFile = object.getContainingFile().getVirtualFile();
        if (schemaFile == null) return;

        final Processor<JsonSchemaObject> processor = schemaObject -> {
          final List<JsonSchemaWalker.Step> steps = skipProperties(JsonSchemaWalker.findPosition(object, false, true));
          JsonSchemaWalker.extractSchemaVariants(object.getProject(), (isName, schema, schemaFile1, steps1) -> {
                                                   if (schemaFile.equals(schemaFile1)) {
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
                                                     final String patternError = schema.getPatternError();
                                                     if (patternError != null && schema.getPattern() != null) {
                                                       final SmartPsiElementPointer<JsonObject> pointer = schema.getPeerPointer();
                                                       final JsonObject element = pointer.getElement();
                                                       if (element != null && element.isValid()) {
                                                         final JsonProperty pattern = element.findProperty("pattern");
                                                         if (pattern != null) {
                                                           error(StringUtil.convertLineSeparators(patternError), pattern.getValue());
                                                         }
                                                       }
                                                     }
                                                   }
                                                 },
                                                 schemaFile, schemaObject, false, steps, true);
          return true;
        };
        JsonSchemaServiceEx.Impl.getEx(object.getProject()).visitSchemaObject(object.getContainingFile().getVirtualFile(), processor);
      }
    }

    private static List<JsonSchemaWalker.Step> skipProperties(List<JsonSchemaWalker.Step> position) {
      final Iterator<JsonSchemaWalker.Step> iterator = position.iterator();
      boolean canSkip = true;
      while (iterator.hasNext()) {
        final JsonSchemaWalker.Step step = iterator.next();
        if (canSkip && step.getTransition() instanceof JsonSchemaWalker.PropertyTransition &&
            "properties".equals(((JsonSchemaWalker.PropertyTransition)step.getTransition()).getName())) {
          iterator.remove();
          canSkip = false;
        } else canSkip = true;
      }
      return position;
    }

    private void checkForEnum(JsonValue value, JsonSchemaObject schema) {
      //enum values + pattern -> don't check enum values
      if (schema.getEnum() == null || schema.getPattern() != null) return;
      final String text = value.getText();
      final List<Object> objects = schema.getEnum();
      for (Object object : objects) {
        if (object.toString().equalsIgnoreCase(text)) return;
      }
      error("Value should be one of: [" + StringUtil.join(objects, o -> o.toString(), ", ") + "]", value);
    }

    private void checkArray(JsonValue value, JsonSchemaObject schema) {
      final JsonArray array = ObjectUtils.tryCast(value, JsonArray.class);
      //noinspection ConstantConditions
      final List<JsonValue> list = array.getValueList();
      if (schema.getMinLength() != null && list.size() < schema.getMinLength()) {
        error("Array is shorter than " + schema.getMinLength(), array);
        return;
      }
      new ArrayItemsChecker().check(array, list, schema);
    }

    private class ArrayItemsChecker {
      private final Map<String, JsonValue> myValueTexts = new HashMap<>();
      private List<JsonValue> myNonUnique;
      private boolean myCheckUnique;

      public void check(JsonArray array, final List<JsonValue> list, final JsonSchemaObject schema) {
        myCheckUnique = schema.isUniqueItems();
        if (schema.getItemsSchema() != null) {
          for (JsonValue arrayValue : list) {
            checkByScheme(arrayValue, schema.getItemsSchema(), new HashSet<>());
            checkUnique(arrayValue);
          }
        } else if (schema.getItemsSchemaList() != null) {
          final Iterator<JsonSchemaObject> iterator = schema.getItemsSchemaList().iterator();
          for (JsonValue arrayValue : list) {
            if (iterator.hasNext()) {
              checkByScheme(arrayValue, iterator.next(), new HashSet<>());
            } else {
              if (!Boolean.TRUE.equals(schema.getAdditionalItemsAllowed())) {
                error("Additional items are not allowed", arrayValue);
              }
            }
            checkUnique(arrayValue);
          }
        } else {
          for (JsonValue arrayValue : list) {
            checkUnique(arrayValue);
          }
        }
        if (myNonUnique != null) {
          for (JsonValue value : myNonUnique) {
            error("Item is not unique", value);
          }
        }
        if (schema.getMinItems() != null && list.size() < schema.getMinItems()) {
          error("Array is shorter than " + schema.getMinItems(), array);
        }
        if (schema.getMaxItems() != null && list.size() > schema.getMaxItems()) {
          error("Array is longer than " + schema.getMaxItems(), array);
        }
      }

      private void checkUnique(JsonValue arrayValue) {
        if (myCheckUnique && myValueTexts.containsKey(arrayValue.getText())) {
          if (myNonUnique == null) myNonUnique = new ArrayList<>();
          myNonUnique.add(arrayValue);
          myNonUnique.add(myValueTexts.get(arrayValue.getText()));
        }
        myValueTexts.put(arrayValue.getText(), arrayValue);
      }
    }

    private void checkString(JsonValue propValue, JsonSchemaObject schema) {
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
          error("Can not check string by pattern because of error: " + StringUtil.convertLineSeparators(schema.getPatternError()), propValue);
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

    private void checkNumber(JsonValue propValue, JsonSchemaObject schema, JsonSchemaType schemaType) {
      Number value;
      if (JsonSchemaType._integer.equals(schemaType)) {
        try {
          value = Integer.valueOf(propValue.getText());
        } catch (NumberFormatException e) {
          error("Integer value expected", propValue);
          return;
        }
      } else {
        try {
          value = Double.valueOf(propValue.getText());
        } catch (NumberFormatException e) {
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

    private void checkMaximum(JsonSchemaObject schema, Number value, JsonValue propertyValue,
                              @NotNull JsonSchemaType propValueType) {
      if (JsonSchemaType._integer.equals(propValueType)) {
        final int intValue = schema.getMaximum().intValue();
        if (Boolean.TRUE.equals(schema.isExclusiveMaximum())) {
          if (value.intValue() >= intValue) {
            error("Greater than an exclusive maximum " + intValue, propertyValue);
          }
        } else {
          if (value.intValue() > intValue) {
            error("Greater than a maximum " + intValue, propertyValue);
          }
        }
      } else {
        final double doubleValue = schema.getMaximum().doubleValue();
        if (Boolean.TRUE.equals(schema.isExclusiveMaximum())) {
          if (value.doubleValue() >= doubleValue) {
            error("Greater than an exclusive maximum " + schema.getMinimum(), propertyValue);
          }
        } else {
          if (value.doubleValue() > doubleValue) {
            error("Greater than a maximum " + schema.getMaximum(), propertyValue);
          }
        }
      }
    }

    private void checkMinimum(JsonSchemaObject schema, Number value, JsonValue propertyValue,
                              @NotNull JsonSchemaType schemaType) {
      if (JsonSchemaType._integer.equals(schemaType)) {
        final int intValue = schema.getMinimum().intValue();
        if (Boolean.TRUE.equals(schema.isExclusiveMinimum())) {
          if (value.intValue() <= intValue) {
            error("Less than an exclusive minimum " + intValue, propertyValue);
          }
        } else {
          if (value.intValue() < intValue) {
            error("Less than a minimum " + intValue, propertyValue);
          }
        }
      } else {
        final double doubleValue = schema.getMinimum().doubleValue();
        if (Boolean.TRUE.equals(schema.isExclusiveMinimum())) {
          if (value.doubleValue() <= doubleValue) {
            error("Less than an exclusive minimum " + schema.getMinimum(), propertyValue);
          }
        } else {
          if (value.doubleValue() < doubleValue) {
            error("Less than a minimum " + schema.getMinimum(), propertyValue);
          }
        }
      }
    }

    private void processAllOf(JsonValue value, JsonSchemaObject schema, Set<String> validatedProperties) {
      final List<JsonSchemaObject> allOf = schema.getAllOf();
      for (JsonSchemaObject object : allOf) {
        checkByScheme(value, object, validatedProperties);
      }
    }

    private void processOneOf(JsonValue value, JsonSchemaObject schema, Set<String> validatedProperties) {
      final List<JsonSchemaObject> oneOf = schema.getOneOf();
      final Map<PsiElement, String> errors = new HashMap<>();
      int cntCorrect = 0;
      boolean validatedPropertiesAdded = false;
      for (JsonSchemaObject object : oneOf) {
        final BySchemaChecker checker = new BySchemaChecker();
        final HashSet<String> local = new HashSet<>();
        checker.checkByScheme(value, object, local);
        if (checker.isCorrect()) {
          if (!validatedPropertiesAdded) {
            validatedPropertiesAdded = true;
            validatedProperties.addAll(local);
          }
          ++ cntCorrect;
        } else {
          if (errors.isEmpty() || notTypeError(value, checker)) {
            errors.clear();
            errors.putAll(checker.getErrors());
          }
        }
      }
      if (cntCorrect == 1) return;
      if (cntCorrect > 0) {
        error("Validates to more than one variant", value);
      } else {
        if (!errors.isEmpty()) {
          for (Map.Entry<PsiElement, String> entry : errors.entrySet()) {
            error(entry.getValue(), entry.getKey());
          }
        }
      }
    }

    private static boolean notTypeError(JsonValue value, BySchemaChecker checker) {
      if (!checker.isHadTypeError()) return true;
      return !checker.getErrors().containsKey(value);
    }

    private void processAnyOf(JsonValue value, JsonSchemaObject schema, Set<String> validatedProperties) {
      final List<JsonSchemaObject> anyOf = schema.getAnyOf();
      final Map<PsiElement, String> errors = new HashMap<>();
      for (JsonSchemaObject object : anyOf) {
        final BySchemaChecker checker = new BySchemaChecker();
        final HashSet<String> local = new HashSet<>();
        checker.checkByScheme(value, object, local);
        if (checker.isCorrect()) {
          validatedProperties.addAll(local);
          return;
        }
        if (errors.isEmpty() && notTypeError(value, checker)) {
          errors.clear();
          errors.putAll(checker.getErrors());
        }
      }
      if (! errors.isEmpty()) {
        for (Map.Entry<PsiElement, String> entry : errors.entrySet()) {
          error(entry.getValue(), entry.getKey());
        }
      }
    }

    private boolean isCorrect() {
      return myErrors.isEmpty();
    }
  }

  @Nullable
  private static JsonSchemaType matchSchemaType(@NotNull JsonSchemaObject schema, @NotNull JsonSchemaType input) {
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
    return null;
  }

  @Nullable
  private static JsonSchemaType getType(JsonValue value) {
    if (value instanceof JsonNullLiteral) return JsonSchemaType._null;
    if (value instanceof JsonBooleanLiteral) return JsonSchemaType._boolean;
    if (value instanceof JsonStringLiteral) return JsonSchemaType._string;
    if (value instanceof JsonArray) return JsonSchemaType._array;
    if (value instanceof JsonObject) return JsonSchemaType._object;
    if (value instanceof JsonNumberLiteral) {
      return isInteger(value.getText()) ? JsonSchemaType._integer : JsonSchemaType._number;
    }
    return null;
  }

  private static boolean isInteger(@NotNull String text) {
    try {
      //noinspection ResultOfMethodCallIgnored
      Integer.parseInt(text);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  // todo no pattern properties at the moment
  /*@Nullable
  private static JsonSchemaObject getChild(JsonSchemaObject current, String name) {
    JsonSchemaObject schema = current.getProperties().get(name);
    if (schema != null) return schema;

    schema = getChildFromList(name, current.getAnyOf());
    if (schema != null) return schema;

    schema = getChildFromList(name, current.getOneOf());
    if (schema != null) return schema;

    final JsonSchemaObject not = current.getNot();
    if (not != null) {
      final JsonSchemaObject notChild = getChild(not, name);
      if (notChild != null) return null;
    }

    if (Boolean.TRUE.equals(current.getAdditionalPropertiesAllowed())) {
      schema = current.getAdditionalPropertiesSchema();
      if (schema == null) return ANY_SCHEMA;
      return schema;
    }
    return null;
  }

  @Nullable
  private static JsonSchemaObject getChildFromList(String name, List<JsonSchemaObject> of) {
    if (of == null) return null;
    JsonSchemaObject schema;
    for (JsonSchemaObject object : of) {
      schema = getChild(object, name);
      if (schema != null) return schema;
    }
    return null;
  }*/
}