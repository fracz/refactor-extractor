package com.jetbrains.edu.learning.newproject.ui;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.edu.learning.EduPluginConfigurator;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseGeneration.StudyProjectGenerator;
import com.jetbrains.edu.learning.newproject.EduCourseProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EduCoursesPanel extends JPanel {
  private static final JBColor LIST_COLOR = new JBColor(Gray.xFF, Gray.x39);
  private JPanel myMainPanel;
  private JPanel myCourseListPanel;
  private JPanel myCoursePanel;
  private JPanel myAdvancedSettingsPlaceholder;
  private JPanel myAdvancedSettings;
  private FilterComponent mySearchField;
  private JEditorPane myDescriptionTextArea;
  private JBLabel myCourseNameLabel;
  private JPanel myTagsPanel;
  private JBScrollPane myInfoScroll;
  private JBList<Course> myCoursesList;
  private LabeledComponent<TextFieldWithBrowseButton> myLocationField;
  private List<Course> myCourses;

  public EduCoursesPanel() {
    setLayout(new BorderLayout());
    add(myMainPanel, BorderLayout.CENTER);
    initUI();
  }


  private void initUI() {
    GuiUtils.replaceJSplitPaneWithIDEASplitter(myMainPanel, true);
    myCourseNameLabel.setBorder(IdeBorderFactory.createEmptyBorder(0, 10, -10, 10));
    myTagsPanel.setBorder(IdeBorderFactory.createEmptyBorder(0, 10, 0, 10));
    myDescriptionTextArea.setBorder(IdeBorderFactory.createEmptyBorder(20, 10, 10, 10));
    myDescriptionTextArea.setEditorKit(UIUtil.getHTMLEditorKit());
    myDescriptionTextArea.setEditable(false);
    myDescriptionTextArea.setPreferredSize(JBUI.size(myCoursePanel.getPreferredSize()));
    myInfoScroll.setBorder(null);
    myCoursesList = new JBList<>();
    myCourses = new StudyProjectGenerator().getCoursesUnderProgress(true, "Getting Available Courses", null);
    updateModel(myCourses);

    ListCellRendererWrapper<Course> renderer = new ListCellRendererWrapper<Course>() {
      @Override
      public void customize(JList list, Course value, int index, boolean selected, boolean hasFocus) {
        setText(value.getName());
        DirectoryProjectGenerator generator = getGenerator(value);
        if (generator != null) {
          setIcon(generator.getLogo());
        }
      }
    };
    myCoursesList.setCellRenderer(renderer);
    myCoursesList.setFixedCellHeight(30);
    myLocationField = createLocationComponent();
    myCoursesList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        Course selectedCourse = myCoursesList.getSelectedValue();
        if (selectedCourse == null) {
          return;
        }
        updateCourseInfoPanel(selectedCourse);
        updateAdvancedSettings(selectedCourse);
      }
    });
    JScrollPane installedScrollPane = ScrollPaneFactory.createScrollPane(myCoursesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    installedScrollPane.setBorder(null);
    myCourseListPanel.add(installedScrollPane, BorderLayout.CENTER);
    Border border = JBUI.Borders.customLine(OnePixelDivider.BACKGROUND, 1, 0, 1, 1);
    myCoursePanel.setBorder(border);
    myCourseListPanel.setBorder(JBUI.Borders.customLine(OnePixelDivider.BACKGROUND, 1, 1, 1, 1));
    HideableDecorator decorator = new HideableDecorator(myAdvancedSettingsPlaceholder, "Advanced Settings", false);
    decorator.setContentComponent(myAdvancedSettings);
    myAdvancedSettings.setBorder(IdeBorderFactory.createEmptyBorder(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));
    UIUtil.setBackgroundRecursively(myCoursesList, LIST_COLOR);
    myDescriptionTextArea.setBackground(UIUtil.getPanelBackground());
    myAdvancedSettingsPlaceholder.setVisible(false);
  }

  private void updateAdvancedSettings(Course selectedCourse) {
    myAdvancedSettingsPlaceholder.setVisible(true);
    myLocationField.getComponent().setText(nameToLocation(selectedCourse.getName()));
    EduPluginConfigurator configurator = EduPluginConfigurator.INSTANCE.forLanguage(selectedCourse.getLanguageById());
    if (configurator == null) {
      return;
    }
    EduCourseProjectGenerator generator = configurator.getEduCourseProjectGenerator();
    if (generator == null) {
      return;
    }
    LabeledComponent<JComponent> component = generator.getLanguageSettingsComponent();
    if (component == null) {
      return;
    }
    myAdvancedSettings.removeAll();
    myAdvancedSettings.add(myLocationField, BorderLayout.NORTH);
    myAdvancedSettings.add(component, BorderLayout.SOUTH);
    UIUtil.mergeComponentsWithAnchor(myLocationField, component);
    myAdvancedSettings.revalidate();
    myAdvancedSettings.repaint();
  }

  private void updateCourseInfoPanel(Course selectedCourse) {
    String courseName = selectedCourse.getName();
    String description = selectedCourse.getDescription();
    myCourseNameLabel.setText(UIUtil.toHtml("<h1>" + courseName + "</h1>"));
    myTagsPanel.removeAll();
    addTags(myTagsPanel, selectedCourse);
    myTagsPanel.revalidate();
    myTagsPanel.repaint();
    myDescriptionTextArea.setText(UIUtil.toHtml(description.replace("\n", "<br>")));
  }

  private static void addTags(JPanel tagsPanel, @NotNull Course course) {
    tagsPanel.add(createTagLabel(course.getLanguageById().getDisplayName()));
    if (course.isAdaptive()) {
      tagsPanel.add(createTagLabel("Adaptive"));
    }
  }

  private static JLabel createTagLabel(String tagText) {
    Border emptyBorder = IdeBorderFactory.createEmptyBorder(3, 5, 3, 5);
    JBLabel label = new JBLabel(tagText);
    label.setOpaque(true);
    label.setBorder(emptyBorder);
    label.setBackground(JBColor.LIGHT_GRAY);
    return label;
  }

  private void updateModel(List<Course> courses) {
    DefaultListModel<Course> listModel = new DefaultListModel<>();
    for (Course course : courses) {
      listModel.addElement(course);
    }
    myCoursesList.setModel(listModel);
  }

  public Course getSelectedCourse() {
    return myCoursesList.getSelectedValue();
  }

  private void createUIComponents() {
    mySearchField = new FilterComponent("Edu.NewCourse", 5, true) {
      @Override
      public void filter() {
        String filter = getFilter();
        List<Course> filtered = new ArrayList<>();
        for (Course course : myCourses) {
          if (accept(filter, course)) {
            filtered.add(course);
          }
        }
        updateModel(filtered);
      }
    };
    UIUtil.setBackgroundRecursively(mySearchField, UIUtil.getTextFieldBackground());
  }

  public boolean accept(String filter, Course course) {
    return course.getName().toLowerCase().contains(filter.toLowerCase()) || filter.isEmpty();
  }

  @Nullable
  private static DirectoryProjectGenerator getGenerator(@NotNull Course course) {
    EduCourseProjectGenerator projectGenerator =
      EduPluginConfigurator.INSTANCE.forLanguage(course.getLanguageById()).getEduCourseProjectGenerator();
    return projectGenerator == null ? null : projectGenerator.getDirectoryProjectGenerator();
  }

  private static LabeledComponent<TextFieldWithBrowseButton> createLocationComponent() {
    TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
    field.addBrowseFolderListener("Select Course Location", "Select course location", null,
                                  FileChooserDescriptorFactory.createSingleFolderDescriptor());
    return LabeledComponent.create(field, "Location", BorderLayout.WEST);
  }

  @NotNull
  private static String nameToLocation(@NotNull String courseName) {
    String name = FileUtil.sanitizeFileName(courseName);
    return FileUtil.findSequentNonexistentFile(new File(ProjectUtil.getBaseDir()), name, "").getAbsolutePath();
  }

  public String getLocationString() {
    return myLocationField.getComponent().getText();
  }
}