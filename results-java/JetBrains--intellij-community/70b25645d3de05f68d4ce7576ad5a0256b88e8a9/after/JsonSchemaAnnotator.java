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

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.jetbrains.jsonSchema.JsonSchemaFileType;
import com.jetbrains.jsonSchema.ide.JsonSchemaService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Irina.Chernushina on 8/31/2015.
 */
public class JsonSchemaAnnotator implements Annotator {
  private static final Key<Set<PsiElement>> ANNOTATED_PROPERTIES = Key.create("JsonSchema.Properties.Annotated");

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element.getContainingFile() == null) return;

    final JsonSchemaObject rootSchema =
      JsonSchemaService.Impl.get(element.getProject()).getSchemaObject(element.getContainingFile().getViewProvider().getVirtualFile());
    if (rootSchema == null || rootSchema.getSchemaFile() == null) return;

    final JsonLikePsiWalker walker = JsonLikePsiWalker.getWalker(element, rootSchema);
    if (walker == null) {
      return;
    }
    new Worker(rootSchema, holder, walker).annotate(element);
  }

  // todo reconsider class names and maybe not so many
  public static class Worker {
    @NotNull private final JsonSchemaObject myRootSchema;
    @NotNull private final AnnotationHolder myHolder;
    @NotNull private final JsonLikePsiWalker myWalker;

    public Worker(@NotNull JsonSchemaObject rootSchema,
                  @NotNull AnnotationHolder holder,
                  @NotNull JsonLikePsiWalker walker) {
      myRootSchema = rootSchema;
      myHolder = holder;
      myWalker = walker;
    }

    public void annotate(@NotNull final PsiElement element) {
      final JsonPropertyAdapter firstProp = myWalker.getParentPropertyAdapter(element);
      if (firstProp == null) {
        final JsonValueAdapter root = findTopLevelElement(myWalker, element);
        if (root != null) {
          checkRootObject(root, myRootSchema);
        }
        return;
      }

      final JsonValueAdapter firstPropValue = firstProp.getValue();
      if (firstPropValue != null) checkFirstPropValue(element, firstProp, firstPropValue);

      final JsonObjectValueAdapter parentObject = firstProp.getParentObject();
      if (parentObject != null && myWalker.isTopJsonElement(parentObject.getDelegate().getParent())) {
        checkRootObject(parentObject, myRootSchema);
      }
      final JsonArrayValueAdapter parentArray = firstProp.getParentArray();
      if (parentArray != null && myWalker.isTopJsonElement(parentArray.getDelegate().getParent())) {
        checkRootObject(parentArray, myRootSchema);
      }
    }

    private void checkFirstPropValue(@NotNull PsiElement element,
                                     @NotNull JsonPropertyAdapter firstProp,
                                     @NotNull JsonValueAdapter firstPropValue) {
      // todo move it somewhere
      final List<JsonSchemaVariantsTreeBuilder.Step> position = myWalker.findPosition(firstProp.getDelegate(), false, true);
      if (position == null || position.isEmpty()) return;

      if (isInsideSchemaDefinitions(element, position)) return;

      final MatchResult result = new JsonSchemaResolver(myRootSchema, false, position).detailedResolve();
      final BySchemaChecker checker = checkByMatchResult(firstPropValue, result, myWalker, myRootSchema);
      if (checker != null) processCheckerResults(checker);
    }

    // does not validate definitions section against general schema --> should be done separately
    private static boolean isInsideSchemaDefinitions(@NotNull PsiElement element, List<JsonSchemaVariantsTreeBuilder.Step> position) {
      if (!JsonSchemaFileType.INSTANCE.equals(element.getContainingFile().getFileType())) return false;
      final JsonSchemaVariantsTreeBuilder.Step firstStep = position.get(0);
      assert firstStep != null;
      return firstStep.isFromObject() && JsonSchemaObject.DEFINITIONS.equals(firstStep.getName());
    }

    private void checkRootObject(@NotNull JsonValueAdapter adapter, @NotNull JsonSchemaObject rootSchema) {
      if (!adapter.isObject() && !adapter.isArray()) return;
      final MatchResult matchResult = new JsonSchemaResolver(rootSchema, rootSchema).detailedResolve();
      final BySchemaChecker checker = checkByMatchResult(adapter, matchResult, myWalker, rootSchema);
      if (checker != null) processCheckerResults(checker);
    }

    private void processCheckerResults(@NotNull BySchemaChecker checker) {
      if (! checker.isCorrect()) {
        for (Map.Entry<PsiElement, String> entry : checker.getErrors().entrySet()) {
          if (checkIfAlreadyProcessed(myHolder, entry.getKey())) continue;
          myHolder.createWarningAnnotation(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  static BySchemaChecker checkByMatchResult(@NotNull JsonValueAdapter elementToCheck,
                                            @NotNull final MatchResult result,
                                            @NotNull JsonLikePsiWalker walker, JsonSchemaObject rootSchema) {
    final List<BySchemaChecker> checkers = new ArrayList<>();
    final Set<String> validatedProperties = new HashSet<>();// todo validated properties seem to should not be there
    if (result.myExcludingSchemas.isEmpty() && result.mySchemas.size() == 1) {
      final BySchemaChecker checker = new BySchemaChecker(rootSchema, walker);
      checker.checkBySchemeBare(elementToCheck, result.mySchemas.get(0), validatedProperties);
      checkers.add(checker);
    } else {
      // todo simplify more
      if (!result.mySchemas.isEmpty()) {
        final BySchemaChecker checker = new BySchemaChecker(rootSchema, walker);
        final List<JsonSchemaObject> filtered = filterSchemaListByType(result.mySchemas, elementToCheck);
        if (filtered.isEmpty()) checker.typeError(elementToCheck.getDelegate());
        else checker.processAnyOf(elementToCheck, filtered, validatedProperties);
        checkers.add(checker);
      }
      if (!result.myExcludingSchemas.isEmpty()) {
        final BySchemaChecker checker = new BySchemaChecker(rootSchema, walker);
        final List<JsonSchemaObject> filtered = filterSchemaListByType(result.myExcludingSchemas, elementToCheck);
        if (filtered.isEmpty()) checker.typeError(elementToCheck.getDelegate());
        else checker.processOneOf(elementToCheck, filtered, validatedProperties);
        checkers.add(checker);
      }
    }
    if (checkers.isEmpty()) return null;

    return checkers.stream()
      .filter(checker -> !checker.isHadTypeError())
      .findFirst()
      .orElse(checkers.get(0));
  }

  @NotNull
  private static List<JsonSchemaObject> filterSchemaListByType(@NotNull final Collection<JsonSchemaObject> collection,
                                                               @NotNull final JsonValueAdapter value) {
    final JsonSchemaType type = JsonSchemaType.getType(value);
    if (type == null) return Collections.emptyList();
    return collection.stream()
      .filter(schema -> typeMakesSense(schema, type))
      .collect(Collectors.toList());
  }

  private static boolean typeMakesSense(@NotNull final JsonSchemaObject schema, @NotNull final JsonSchemaType type) {
    if (matchSchemaType(schema, type) != null) return true;
    if (schema.getEnum() != null && (JsonSchemaType._integer.equals(type) ||
                                     JsonSchemaType._number.equals(type) || JsonSchemaType._boolean.equals(type) ||
                                     JsonSchemaType._string.equals(type))) return true;
    return false;
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
    if (!schema.getProperties().isEmpty() && JsonSchemaType._object.equals(input)) return JsonSchemaType._object;
    return null;
  }

  private static JsonValueAdapter findTopLevelElement(@NotNull JsonLikePsiWalker walker, @NotNull PsiElement element) {
    final Ref<PsiElement> ref = new Ref<>();
    PsiTreeUtil.findFirstParent(element, el -> {
      final boolean isTop = walker.isTopJsonElement(el);
      if (!isTop) ref.set(el);
      return isTop;
    });
    return ref.isNull() ? null : walker.createValueAdapter(ref.get());
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

  private static class BySchemaChecker {
    @NotNull private final JsonSchemaObject myRootSchema;
    private final JsonLikePsiWalker myWalker;
    private final Map<PsiElement, String> myErrors;
    private boolean myHadTypeError;

    public BySchemaChecker(@NotNull JsonSchemaObject rootSchema, @NotNull JsonLikePsiWalker walker) {
      myRootSchema = rootSchema;
      myWalker = walker;
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

    void typeError(final @NotNull PsiElement value) {
      error("Type is not allowed", value);
      myHadTypeError = true;
    }

    private void checkBySchemeBare(@NotNull JsonValueAdapter value, @NotNull JsonSchemaObject schema, Set<String> validatedProperties) {
      final JsonSchemaType type = JsonSchemaType.getType(value);
      if (type == null) {
        //typeError(value.getDelegate());
      } else {
        JsonSchemaType schemaType = matchSchemaType(schema, type);
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
          checkObject(value, schema, validatedProperties);
          checkForEnum(value.getDelegate(), schema);
        }
      }

      if (schema.getNot() != null) {
        final BySchemaChecker checker = checkByInnerSchema(schema.getNot(), value);
        if (checker == null || checker.isCorrect()) error("Validates against 'not' schema", value.getDelegate());
      }
    }

    @Nullable
    private BySchemaChecker checkByInnerSchema(@NotNull JsonSchemaObject schema, @NotNull JsonValueAdapter object) {
      final MatchResult matchResult = new JsonSchemaResolver(myRootSchema, schema).detailedResolve();
      return checkByMatchResult(object, matchResult, myWalker, myRootSchema);
    }

    private void checkByInnerSchemaRecordErrors(@NotNull JsonSchemaObject schema, @NotNull JsonValueAdapter object) {
      final BySchemaChecker checker = checkByInnerSchema(schema, object);
      if (checker != null) {
        myHadTypeError = checker.isHadTypeError();
        myErrors.putAll(checker.getErrors());
      }
    }

    private void checkObject(JsonValueAdapter value, JsonSchemaObject schema, Set<String> validatedProperties) {
      final Map<String, JsonSchemaObject> properties = schema.getProperties();
      final JsonObjectValueAdapter object = value.getAsObject();
      if (object == null) return;
      //noinspection ConstantConditions
      final List<JsonPropertyAdapter> propertyList = object.getPropertyList();
      final Set<String> set = new HashSet<>();
      for (JsonPropertyAdapter property : propertyList) {
        set.add(property.getName());
        final JsonValueAdapter adapter = property.getValue();
        if (adapter == null) continue;
        final JsonSchemaObject propertySchema = properties.get(property.getName());
        if (propertySchema != null) {
          checkByInnerSchemaRecordErrors(propertySchema, adapter);
        }
        else {
          final JsonSchemaObject patternSchema = schema.getMatchingPatternPropertySchema(StringUtil.notNullize(property.getName()));
          if (patternSchema != null) {
            checkByInnerSchemaRecordErrors(patternSchema, adapter);
          } else if (schema.getAdditionalPropertiesSchema() != null) {
            checkByInnerSchemaRecordErrors(schema.getAdditionalPropertiesSchema(), adapter);
          }
          else if (!Boolean.TRUE.equals(schema.getAdditionalPropertiesAllowed()) && !validatedProperties.contains(property.getName())) {
            error("Property '" + property.getName() + "' is not allowed", property.getDelegate());
          }
        }
        validatedProperties.add(property.getName());
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
              checkByInnerSchemaRecordErrors(entry.getValue(), value);
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

        final List<JsonSchemaVariantsTreeBuilder.Step> steps = skipProperties(JsonOriginalPsiWalker.INSTANCE.findPosition(object, false, true));
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
        } else canSkip = true;
      }
      return position;
    }

    private void checkForEnum(PsiElement value, JsonSchemaObject schema) {
      //enum values + pattern -> don't check enum values
      if (schema.getEnum() == null || schema.getPattern() != null) return;
      final String text = StringUtil.notNullize(value.getText());
      final List<Object> objects = schema.getEnum();
      for (Object object : objects) {
        if (myWalker.onlyDoubleQuotesForStringLiterals()) {
          if (object.toString().equalsIgnoreCase(text)) return;
        } else {
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
      new ArrayItemsChecker().check(value, elements, schema);
    }

    private class ArrayItemsChecker {
      private final Map<String, JsonValueAdapter> myValueTexts = new HashMap<>();
      private List<JsonValueAdapter> myNonUnique;
      private boolean myCheckUnique;

      public void check(@NotNull JsonValueAdapter array, @NotNull final List<JsonValueAdapter> list, final JsonSchemaObject schema) {
        myCheckUnique = schema.isUniqueItems();
        if (schema.getItemsSchema() != null) {
          for (JsonValueAdapter arrayValue : list) {
            checkByInnerSchemaRecordErrors(schema.getItemsSchema(), arrayValue);
            checkUnique(arrayValue);
          }
        } else if (schema.getItemsSchemaList() != null) {
          final Iterator<JsonSchemaObject> iterator = schema.getItemsSchemaList().iterator();
          for (JsonValueAdapter arrayValue : list) {
            if (iterator.hasNext()) {
              checkByInnerSchemaRecordErrors(iterator.next(), arrayValue);
            } else {
              if (!Boolean.TRUE.equals(schema.getAdditionalItemsAllowed())) {
                error("Additional items are not allowed", arrayValue.getDelegate());
              }
            }
            checkUnique(arrayValue);
          }
        } else {
          for (JsonValueAdapter arrayValue : list) {
            checkUnique(arrayValue);
          }
        }
        if (myNonUnique != null) {
          for (JsonValueAdapter value : myNonUnique) {
            error("Item is not unique", value.getDelegate());
          }
        }
        if (schema.getMinItems() != null && list.size() < schema.getMinItems()) {
          error("Array is shorter than " + schema.getMinItems(), array.getDelegate());
        }
        if (schema.getMaxItems() != null && list.size() > schema.getMaxItems()) {
          error("Array is longer than " + schema.getMaxItems(), array.getDelegate());
        }
      }

      private void checkUnique(JsonValueAdapter arrayValue) {
        final String text = arrayValue.getDelegate().getText();
        if (myCheckUnique && myValueTexts.containsKey(text)) {
          if (myNonUnique == null) myNonUnique = new ArrayList<>();
          myNonUnique.add(arrayValue);
          myNonUnique.add(myValueTexts.get(text));
        }
        myValueTexts.put(text, arrayValue);
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

    private void checkNumber(PsiElement propValue, JsonSchemaObject schema, JsonSchemaType schemaType) {
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

    private void checkMaximum(JsonSchemaObject schema, Number value, PsiElement propertyValue,
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

    private void checkMinimum(JsonSchemaObject schema, Number value, PsiElement propertyValue,
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

    private void processOneOf(@NotNull JsonValueAdapter value, List<JsonSchemaObject> oneOf, Set<String> validatedProperties) {
      final Map<PsiElement, String> errors = new HashMap<>();
      int cntCorrect = 0;
      boolean validatedPropertiesAdded = false;
      for (JsonSchemaObject object : oneOf) {
        // skip it if smthg JS awaited, we do not process it currently todo: revisit
        if (object.isShouldValidateAgainstJSType()) continue;

        final BySchemaChecker checker = new BySchemaChecker(myRootSchema, myWalker);
        final HashSet<String> local = new HashSet<>();
        checker.checkBySchemeBare(value, object, local);
        if (checker.isCorrect()) {
          errors.clear();
          if (!validatedPropertiesAdded) {
            validatedPropertiesAdded = true;
            validatedProperties.addAll(local);
          }
          ++ cntCorrect;
        } else {
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
      } else {
        if (!errors.isEmpty()) {
          for (Map.Entry<PsiElement, String> entry : errors.entrySet()) {
            error(entry.getValue(), entry.getKey());
          }
        }
      }
    }

    private static boolean notTypeError(PsiElement value, BySchemaChecker checker) {
      if (!checker.isHadTypeError()) return true;
      return !checker.getErrors().containsKey(value);
    }

    private void processAnyOf(@NotNull JsonValueAdapter value, List<JsonSchemaObject> anyOf, Set<String> validatedProperties) {
      final Map<PsiElement, String> errors = new HashMap<>();
      for (JsonSchemaObject object : anyOf) {
        final BySchemaChecker checker = new BySchemaChecker(myRootSchema, myWalker);
        final HashSet<String> local = new HashSet<>();
        checker.checkBySchemeBare(value, object, local);
        if (checker.isCorrect()) {
          validatedProperties.addAll(local);
          return;
        }
        if (errors.isEmpty() && notTypeError(value.getDelegate(), checker)) {
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
}