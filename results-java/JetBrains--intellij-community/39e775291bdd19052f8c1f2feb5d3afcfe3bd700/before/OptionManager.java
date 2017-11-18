/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.intellij.openapi.module;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OptionManager {
  /**
   * Removes a custom option from this module.
   *
   * @param key the name of the custom option.
   */
  void clearOption(@NotNull Key<String> key);

  /**
   * Sets a custom option for this module.
   *
   * @param key the name of the custom option.
   * @param value the value of the custom option.
   */
  void setOption(@NotNull Key<String> key, @NotNull String value);

  /**
   * Gets the value of a custom option for this module.
   *
   * @param key the name of the custom option.
   * @return the value of the custom option, or null if no value has been set.
   */
  @Nullable
  String getOptionValue(@NotNull Key<String> key);
}