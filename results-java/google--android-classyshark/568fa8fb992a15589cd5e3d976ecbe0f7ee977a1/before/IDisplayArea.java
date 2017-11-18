/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.gui.panel.displayarea;

import com.google.classyshark.silverghost.translator.Translator;
import java.awt.Component;
import java.util.List;

public interface IDisplayArea {
    Component onAddComponentToPane();

    void displayClassNames(List<String> classNamesToShow,
                           String inputText);

    void displayClass(List<Translator.ELEMENT> displayedClassTokens, String classString);

    void displayClass(String classString);

    void displaySharkey();

    void displayError();

    void displaySearches(List<String> filteredClassNames, List<Translator.ELEMENT> displayedManifestSearchResultsTokens, String textFromTypingArea);
}