/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.errorprone.bugpatterns.testdata;

import com.google.errorprone.annotations.RestrictedApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Example for @link{com.google.errorprone.bugpatterns.RestrictedApiCheckerTest}. */
public class RestrictedApiMethods {

  public int normalMethod() {
    return 0;
  }

  @RestrictedApi(
    explanation = "lorem",
    whitelistAnnotations = {Whitelist.class},
    whitelistWithWarningAnnotations = {WhitelistWithWarning.class},
    link = ""
  )
  public RestrictedApiMethods(int restricted) {}

  @RestrictedApi(
    explanation = "lorem",
    whitelistAnnotations = {Whitelist.class},
    whitelistWithWarningAnnotations = {WhitelistWithWarning.class},
    link = "",
    allowedOnPath = ".*testsuite/.*"
  )
  public int restrictedMethod() {
    return 1;
  }

  @RestrictedApi(
    explanation = "lorem",
    whitelistAnnotations = {Whitelist.class},
    whitelistWithWarningAnnotations = {WhitelistWithWarning.class},
    link = ""
  )
  public static int restrictedStaticMethod() {
    return 2;
  }
}

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@interface Whitelist {}

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@interface WhitelistWithWarning {}