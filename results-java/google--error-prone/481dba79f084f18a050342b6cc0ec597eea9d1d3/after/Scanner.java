/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.errorprone;

import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Suppressable;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO(eaftan): I'm worried about this performance of this code,
 * specifically the part that handles SuppressWarnings.  We should
 * profile it and see where the hotspots are.
 *
 * @author alexeagle@google.com (Alex Eagle)
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class Scanner extends TreePathScanner<Void, VisitorState> {

  public Set<String> suppressions = new HashSet<String>();

  /**
   * Scan a tree from a position identified by a TreePath.
   */
  @Override
  public Void scan(TreePath path, VisitorState state) {
    /**
     * We maintain a list of suppressed warnings for the current node. When we
     * explore a new node, we have to extend the suppression set with any new
     * suppressed warnings.  We also have to retain the previous suppression set
     * so that we can reinstate it when we move up the tree.
     *
     * We avoid copying the suppression set if the next node to explore does not
     * have any suppressed warnings.  This is the common case.
     */
    Set<String> newSuppressions = null;
    Set<String> prevSuppressions = suppressions;
    Symbol sym = ASTHelpers.getSymbol(path.getLeaf());
    if (sym != null) {
      newSuppressions = extendSuppressionSet(sym, state.getSymtab().suppressWarningsType,
          suppressions);
      if (newSuppressions != null) {
        suppressions = newSuppressions;
      }
    }

    try {
      return super.scan(path, state);
    } finally {
      suppressions = prevSuppressions;
    }
  }

  /**
   * Scan a single node.
   * The current path is updated for the duration of the scan.
   */
  @Override
  public Void scan(Tree tree, VisitorState state) {
    if (tree == null) {
      return null;
    }

    /**
     * We maintain a list of suppressed warnings for the current node. When we
     * explore a new node, we have to extend the suppression set with any new
     * suppressed warnings.  We also have to retain the previous suppression set
     * so that we can reinstate it when we move up the tree.
     *
     * We avoid copying the suppression set if the next node to explore does not
     * have any suppressed warnings.  This is the common case.
     */
    Set<String> newSuppressions = null;
    Set<String> prevSuppressions = suppressions;
    Symbol sym = ASTHelpers.getSymbol(tree);
    if (sym != null) {
      newSuppressions = extendSuppressionSet(sym, state.getSymtab().suppressWarningsType,
          suppressions);
      if (newSuppressions != null) {
        suppressions = newSuppressions;
      }
    }

    try {
      return super.scan(tree, state);
    } finally {
      suppressions = prevSuppressions;
    }

  }

  /**
   * Extends a set of suppressed warnings with the contents of any SuppressWarnings annotations
   * on the given symbol.  Does not mutate the passed-in set of suppressions.  If there were
   * additional warnings to suppress, it returns a copy of the passed-in set with the new warnings
   * added.  If there were no additional warnings to suppress, it returns null.
   *
   * @param sym The possibly-annotated symbol
   * @param suppressWarningsType The type of the SuppressWarnings annotation
   * @param suppressions The set of currently-suppressed warnings
   * @return A new set with additional warnings added, if there were any new ones. Null otherwise.
   */
  private Set<String> extendSuppressionSet(Symbol sym, Type suppressWarningsType,
      Set<String> suppressions) {
    boolean copied = false;
    Set<String> newSuppressions = null;

    // Iterate over annotations on this symbol, looking for SuppressWarnings
    for (Attribute.Compound attr : sym.getAnnotationMirrors()) {
      if (attr.type.tsym == suppressWarningsType.tsym) {
        for (List<Pair<MethodSymbol,Attribute>> v = attr.values;
            v.nonEmpty(); v = v.tail) {
          Pair<MethodSymbol,Attribute> value = v.head;
          if (value.fst.name.toString().equals("value"))
            if (value.snd instanceof Attribute.Array) {  // SuppressWarnings takes an array
              for (Attribute suppress : ((Attribute.Array) value.snd).values) {
                if (!copied) {
                  newSuppressions = new HashSet<String>(suppressions);
                  copied = true;
                }
                // TODO(eaftan): check return value to see if this was a new warning?
                newSuppressions.add((String) suppress.getValue());
              }
            } else {
              throw new RuntimeException("Expected SuppressWarnings annotation to take array type");
            }
        }
      }
    }

    return newSuppressions;
  }

  /**
   * Returns true if any of the warning IDs in the collection are in the set of current
   * suppressions from scanning down the AST.
   *
   * @param suppressable holds a collection of warning IDs
   */
  protected boolean isSuppressed(Suppressable suppressable) {
    return !Collections.disjoint(suppressable.getAllNames(), suppressions);
  }

  protected <T extends Tree> void reportMatch(Description description, T match, VisitorState state) {
    if (description == null || description == Description.NO_MATCH) {
      return;
    }
    state.getMatchListener().onMatch(match);
    state.getDescriptionListener().onDescribed(description);
  }
}