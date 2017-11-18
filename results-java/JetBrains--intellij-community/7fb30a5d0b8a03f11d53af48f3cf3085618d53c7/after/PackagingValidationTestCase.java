package com.intellij.compiler.artifacts;

import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.ui.ArtifactProblemQuickFix;
import com.intellij.packaging.ui.ArtifactValidationManager;
import com.intellij.packaging.ui.PackagingEditorContext;
import com.intellij.openapi.util.text.StringUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author nik
 */
public abstract class PackagingValidationTestCase extends PackagingElementsTestCase {
  protected PackagingValidationTestCase() {
    mySetupModule = true;
  }

  protected MockArtifactValidationManager validate(CompositePackagingElement<?> root, final ArtifactType artifactType) {
    final MockArtifactValidationManager validationManager = new MockArtifactValidationManager();
    final Artifact artifact = addArtifact("artifact", artifactType, root);
    artifactType.checkRootElement(root, artifact, validationManager);
    return validationManager;
  }


  protected class MockArtifactValidationManager implements ArtifactValidationManager {
    private List<String> myProblems = new ArrayList<String>();
    private Map<String, ArtifactProblemQuickFix> myQuickFixes = new THashMap<String, ArtifactProblemQuickFix>();

    public PackagingEditorContext getContext() {
      return new MockPackagingEditorContext();
    }

    public void registerProblem(@NotNull String message) {
      myProblems.add(message);
    }

    public void registerProblem(@NotNull String message, @Nullable ArtifactProblemQuickFix quickFix) {
      myProblems.add(message);
      if (quickFix != null) {
        myQuickFixes.put(message, quickFix);
      }
    }

    public void assertNoProblems() {
      assertProblems();
    }

    public void assertProblems(String... expectedMessages) {
      Set<String> expected = new THashSet<String>(Arrays.asList(expectedMessages));
      outer:
      for (String problem : myProblems) {
        for (String message : expected) {
          if (problem.contains(message)) {
            expected.remove(message);
            continue outer;
          }
        }
        fail("Unexpected problem: " + problem);
      }
      if (!expected.isEmpty()) {
        fail("The following problems are not reported: " + StringUtil.join(expected, "; "));
      }
    }
  }
}