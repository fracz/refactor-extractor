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
package com.intellij.testFramework;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ui.tree.TreeUtil;
import org.junit.Before;

import javax.swing.tree.TreePath;

/**
 * @author Konstantin Bulenkov
 */
public abstract class FileStructureTestBase extends CodeInsightFixtureTestCase {
  protected FileStructureTestFixture myPopupFixture;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setupStructurePopup();
  }

  protected void setupStructurePopup() {
    myFixture.configureByFile(getFileName(getFileExtension()));
    myPopupFixture = new FileStructureTestFixture(myFixture.getProject(), myFixture.getEditor(), getFile());
    myPopupFixture.update();
  }

  protected abstract String getFileExtension();

  @Override
  public void tearDown() throws Exception {
    try {
      if (myPopupFixture != null) {
        myPopupFixture.dispose();
        myPopupFixture = null;
      }
    }
    finally {
      super.tearDown();
    }
  }

  private String getFileName(String ext) {
    return getTestName(false) + (StringUtil.isEmpty(ext) ? "" : "." + ext);
  }

  protected String getTreeFileName() {
    return getFileName("tree");
  }

  protected void checkTree(String filter) {
    myPopupFixture.getPopup().setSearchFilterForTests(filter);
    myPopupFixture.getBuilder().refilter(null, false, true);
    myPopupFixture.getBuilder().queueUpdate();
    TreeUtil.selectPath(myPopupFixture.getTree(), (TreePath)myPopupFixture.getSpeedSearch().findElement(filter));
    checkTree();
  }

  protected void checkTree() {
    assertSameLinesWithFile(getTestDataPath() + "/" + getTreeFileName(), PlatformTestUtil.print(myPopupFixture.getTree(), true).trim());
  }
}