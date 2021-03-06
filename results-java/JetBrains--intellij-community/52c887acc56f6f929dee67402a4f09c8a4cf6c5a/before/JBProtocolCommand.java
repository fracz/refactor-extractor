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
package com.intellij.openapi.application;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public abstract class JBProtocolCommand {
  public static final ExtensionPointName<JBProtocolCommand> EP_NAME = new ExtensionPointName<JBProtocolCommand>("com.intellij.jbProtocolCommand");

  @NotNull
  public abstract String getCommandName();

  public abstract void perform(String target, Map<String, String> parameters);

  @Nullable
  public static JBProtocolCommand findCommand(@Nullable String commandName) {
    if (commandName != null) {
      for (JBProtocolCommand command : EP_NAME.getExtensions()) {
        if (command.getCommandName().equals(commandName)) {
          return command;
        }
      }
    }
    return null;
  }
}