/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.ide.util.importProject;

import com.intellij.facet.impl.autodetecting.facetsTree.DetectedFacetsTreeComponent;
import com.intellij.facet.impl.ui.FacetDetectionProcessor;
import com.intellij.ide.util.newProjectWizard.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.AbstractStepWithProgress;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nik
 */
public abstract class FacetDetectionStep extends AbstractStepWithProgress<Map<ModuleDescriptor, Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>>>>
  implements ProjectFromSourcesBuilder.ProjectConfigurationUpdater {
  private final Icon myIcon;
  private final ModuleType myModuleType;
  private List<File> myLastRoots = null;
  private final DetectedFacetsTreeComponent myDetectedFacetsComponent;
  private JPanel myMainPanel;
  private JPanel myFacetsTreePanel;
  private JLabel myFacetsDetectedLabel;

  public FacetDetectionStep(final Icon icon, ModuleType moduleType) {
    super(ProjectBundle.message("message.text.stop.searching.for.facets", ApplicationNamesInfo.getInstance().getProductName()));
    myIcon = icon;
    myModuleType = moduleType;
    myDetectedFacetsComponent = new DetectedFacetsTreeComponent();
  }

  public void updateDataModel() {
  }

  protected boolean shouldRunProgress() {
    return myLastRoots == null || !Comparing.haveEqualElements(myLastRoots, getRoots());
  }

  protected String getProgressText() {
    return ProjectBundle.message("progress.text.searching.facets");
  }

  protected JComponent createResultsPanel() {
    JPanel mainPanel = myDetectedFacetsComponent.getMainPanel();
    myFacetsTreePanel.add(ScrollPaneFactory.createScrollPane(mainPanel), BorderLayout.CENTER);
    return myMainPanel;
  }

  protected Map<ModuleDescriptor, Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>>> calculate() {
    myLastRoots = getRoots();

    ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();

    Map<ModuleDescriptor, Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>>> result = new HashMap<ModuleDescriptor, Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>>>();
    for (ModuleDescriptor moduleDescriptor : getModuleDescriptors()) {

      Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>> root2Facets = new HashMap<File, List<FacetDetectionProcessor.DetectedFacetInfo>>();
      for (File root : moduleDescriptor.getContentRoots()) {
        FacetDetectionProcessor processor = new FacetDetectionProcessor(progressIndicator, myModuleType);
        processor.process(root);
        List<FacetDetectionProcessor.DetectedFacetInfo> facets = processor.getDetectedFacetsInfos();
        if (!facets.isEmpty()) {
          root2Facets.put(root, facets);
        }
      }

      if (!root2Facets.isEmpty()) {
        result.put(moduleDescriptor, root2Facets);
      }
    }

    return result;
  }

  protected abstract List<ModuleDescriptor> getModuleDescriptors();

  private List<File> getRoots() {
    List<File> roots = new ArrayList<File>();
    for (ModuleDescriptor moduleDescriptor : getModuleDescriptors()) {
      roots.addAll(moduleDescriptor.getContentRoots());
    }
    return roots;
  }

  protected void onFinished(final Map<ModuleDescriptor, Map<File, List<FacetDetectionProcessor.DetectedFacetInfo>>> result, final boolean canceled) {
    myDetectedFacetsComponent.clear();
    for (ModuleDescriptor moduleDescriptor : result.keySet()) {
      myDetectedFacetsComponent.addFacets(moduleDescriptor, result.get(moduleDescriptor));
    }
    myDetectedFacetsComponent.createTree();
    if (result.isEmpty()) {
      myFacetsDetectedLabel.setText(ProjectBundle.message("label.text.no.facets.detected"));
    }
    else {
      myFacetsDetectedLabel.setText(ProjectBundle.message("label.text.the.following.facets.are.detected"));
    }
  }

  public Icon getIcon() {
    return myIcon;
  }

  @NonNls
  public String getHelpId() {
    return "reference.dialogs.new.project.fromCode.facets";
  }

  public void updateModule(final ModuleDescriptor descriptor, final Module module, final ModifiableRootModel rootModel) {
    myDetectedFacetsComponent.createFacets(descriptor, module, rootModel);
  }
}