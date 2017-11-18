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
package com.jetbrains.python.commandInterface.swingView;

import java.awt.event.ActionEvent;

/**
 * "Execute command" action
 *
 * @author Ilya.Kazakevich
 */
final class ExecutionKeyStrokeAction extends KeyStrokeAction {
  ExecutionKeyStrokeAction() {
    super(KeyStrokeInfo.EXECUTION);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    myPresenter.executionRequested();
  }
}