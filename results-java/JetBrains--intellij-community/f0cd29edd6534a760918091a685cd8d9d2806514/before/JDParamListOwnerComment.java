/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package com.intellij.psi.impl.source.codeStyle.javadoc;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JDParamListOwnerComment extends JDComment {
  protected List<NameDesc> myParamsList;

  public JDParamListOwnerComment(@NotNull CommentFormatter formatter) {
    super(formatter);
  }

  @Override
  protected void generateSpecial(@NotNull String prefix, @NotNull StringBuilder sb) {
     if (myParamsList != null) {
      int before = sb.length();
      generateList(prefix, sb, myParamsList, JDTag.PARAM.getWithEndWhitespace(),
                   myFormatter.getSettings().JD_ALIGN_PARAM_COMMENTS,
                   myFormatter.getSettings().JD_KEEP_EMPTY_PARAMETER,
                   myFormatter.getSettings().JD_PARAM_DESCRIPTION_ON_NEW_LINE
      );

      int size = sb.length() - before;
      if (size > 0 && myFormatter.getSettings().JD_ADD_BLANK_AFTER_PARM_COMMENTS) {
        sb.append(prefix);
        sb.append('\n');
      }
    }
  }

  @Nullable
  public NameDesc getParameter(@Nullable String name) {
    return getNameDesc(name, myParamsList);
  }

  public void addParameter(@NotNull String name, @Nullable String description) {
    if (myParamsList == null) {
      myParamsList = ContainerUtilRt.newArrayList();
    }
    myParamsList.add(new NameDesc(name, description));
  }

  @Nullable
  private static NameDesc getNameDesc(@Nullable String name, @Nullable List<NameDesc> list) {
    if (list == null) return null;
    for (NameDesc aList : list) {
      if (aList.name.equals(name)) {
        return aList;
      }
    }
    return null;
  }

  /**
   * Generates parameters or exceptions
   *
   */
  protected void generateList(@NotNull String prefix,
                              @NotNull StringBuilder sb,
                              @NotNull List<NameDesc> list,
                              @NotNull String tag,
                              boolean align_comments,
                              boolean generate_empty_tags,
                              boolean wrapDescription)
  {
    int max = 0;

    if (align_comments && !wrapDescription) {
      for (NameDesc nd: list) {
        int currentLength = nd.name.length();
        if (isNull(nd.desc) && !generate_empty_tags) continue;
        //finding longest parameter length
        if (currentLength > max) {
          max = currentLength;
        }
      }
    }

    StringBuilder fill = new StringBuilder(prefix.length() + tag.length() + max + 1);
    fill.append(prefix);
    StringUtil.repeatSymbol(fill, ' ', max + 1 + tag.length());

    String wrapParametersPrefix = prefix + getContinuationIndent();
    for (NameDesc nd : list) {
      if (isNull(nd.desc) && !generate_empty_tags) continue;
      if (wrapDescription && !isNull(nd.desc)) {
        sb.append(prefix).append(tag).append(nd.name).append("\n");
        sb.append(myFormatter.getParser().formatJDTagDescription(nd.desc, wrapParametersPrefix));
      }
      else if (align_comments) {
        int spacesNumber = max + 1 - nd.name.length();
        String spaces = StringUtil.repeatSymbol(' ', Math.max(0, spacesNumber));
        String firstLinePrefix = prefix + tag + nd.name + spaces;
        sb.append(myFormatter.getParser().formatJDTagDescription(nd.desc, firstLinePrefix, fill));
      }
      else {
        String description = (nd.desc == null) ? "" : nd.desc;
        sb.append(myFormatter.getParser().formatJDTagDescription(tag + nd.name + " " + description,
                                                                 prefix,
                                                                 getContinuationPrefix(prefix)));
      }
    }
  }
}