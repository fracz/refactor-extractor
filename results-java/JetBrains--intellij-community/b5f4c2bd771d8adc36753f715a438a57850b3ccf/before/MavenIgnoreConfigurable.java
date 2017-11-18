package org.jetbrains.idea.maven.project;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.utils.MavenUIUtil;
import org.jetbrains.idea.maven.utils.Strings;

import javax.swing.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Vladislav.Kaznacheev
 */
public class MavenIgnoreConfigurable implements Configurable {
  private JPanel panel;
  private ElementsChooser<VirtualFile> myFileChooser;
  private JTextArea myMaskEditor;

  private final MavenProjectsManager myManager;
  private final Collection<VirtualFile> myOriginalFiles;
  private final String myOriginalMasks;

  private static final char SEPARATOR = ',';

  public MavenIgnoreConfigurable(MavenProjectsManager manager) {
    myManager = manager;

    myOriginalFiles = new THashSet<VirtualFile>();
    for (MavenProject each : myManager.getProjects()) {
      if (myManager.getIgnoredState(each)) {
        myOriginalFiles.add(each.getFile());
      }
    }

    myOriginalMasks = Strings.detokenize(myManager.getIgnoredFilesPatterns(), SEPARATOR);
  }

  private void createUIComponents() {
    myFileChooser = new ElementsChooser<VirtualFile>(true) {
      protected String getItemText(@NotNull final VirtualFile virtualFile) {
        return virtualFile.getPath();
      }
    };
  }

  @Nls
  public String getDisplayName() {
    return ProjectBundle.message("maven.tab.ignore");
  }

  @Nullable
  public Icon getIcon() {
    return null;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return "reference.settings.project.maven.ignored.files";
  }

  public JComponent createComponent() {
    return panel;
  }

  public boolean isModified() {
    return !MavenUIUtil.equalAsSets(myOriginalFiles, myFileChooser.getMarkedElements()) ||
           !myOriginalMasks.equals(myMaskEditor.getText());
  }

  public void apply() throws ConfigurationException {
    List<VirtualFile> ignoredFiles = myFileChooser.getMarkedElements();
    List<MavenProject> ignoredProjects = new ArrayList<MavenProject>();
    List<MavenProject> unignoredProjects = new ArrayList<MavenProject>();

    for (MavenProject each : myManager.getProjects()) {
      if (ignoredFiles.contains(each.getFile())) {
        ignoredProjects.add(each);
      } else {
        unignoredProjects.add(each);
      }
    }

    myManager.setIgnoredState(ignoredProjects, true);
    myManager.setIgnoredState(unignoredProjects, false);
    myManager.setIgnoredFilesPatterns(Strings.tokenize(myMaskEditor.getText(), Strings.WHITESPACE + SEPARATOR));
  }

  public void reset() {
    MavenUIUtil.addElements(myFileChooser, myManager.getProjectsFiles(), myOriginalFiles, new Comparator<VirtualFile>() {
      public int compare(VirtualFile o1, VirtualFile o2) {
        //noinspection ConstantConditions
        return o1.getParent().getPath().compareTo(o2.getParent().getPath());
      }
    });

    myMaskEditor.setText(myOriginalMasks);
  }

  public void disposeUIResources() {
  }
}