/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.ide.util.projectWizard;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Computable;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Eugene Zhuravlev
 *         Date: Oct 6, 2004
 */
public class ProjectWizardStepFactoryImpl extends ProjectWizardStepFactory {
  private final MultiMap<ModuleType, AddSupportStepsProvider> myStepsProviders = new MultiMap<ModuleType, AddSupportStepsProvider>();

  public ModuleWizardStep createNameAndLocationStep(WizardContext wizardContext, JavaModuleBuilder builder, ModulesProvider modulesProvider, Icon icon, String helpId) {
    return new NameLocationStep(wizardContext, builder, modulesProvider, icon, helpId);
  }

  public ModuleWizardStep createNameAndLocationStep(final WizardContext wizardContext) {
    return new ProjectNameStep(wizardContext);
  }

  /**
   * @deprecated
   */
  public ModuleWizardStep createOutputPathPathsStep(ModuleWizardStep nameAndLocationStep, JavaModuleBuilder builder, Icon icon, String helpId) {
    return new OutputPathsStep((NameLocationStep)nameAndLocationStep, builder, icon, helpId);
  }

  public ModuleWizardStep createSourcePathsStep(ModuleWizardStep nameAndLocationStep, JavaModuleBuilder builder, Icon icon, String helpId) {
    return null;
  }

  public ModuleWizardStep createProjectJdkStep(WizardContext context,
                                               final JavaModuleBuilder builder,
                                               final Computable<Boolean> isVisible,
                                               final Icon icon,
                                               final String helpId) {
    return createProjectJdkStep(context, null, builder, isVisible, icon, helpId);
  }

  public ModuleWizardStep createProjectJdkStep(WizardContext context,
                                               SdkType type,
                                               final JavaModuleBuilder builder,
                                               final Computable<Boolean> isVisible,
                                               final Icon icon,
                                               @NonNls final String helpId) {
    return new ProjectJdkForModuleStep(context, type){
      public void updateDataModel() {
        super.updateDataModel();
        builder.setModuleJdk(getJdk());
      }

      public boolean isStepVisible() {
        return isVisible.compute().booleanValue();
      }

      public Icon getIcon() {
        return icon;
      }

      public String getHelpId() {
        return helpId;
      }
    };
  }

  public AddSupportStep createLoadJarsStep(AddSupportContext context, String title, String description, Icon icon) {
    return new LoadJarsStep<AddSupportContext>(context, title, description, icon);
  }

  public void registerAddSupportProvider(final ModuleType moduleType, AddSupportStepsProvider provider) {
    myStepsProviders.putValue(moduleType, provider);
  }

  @NotNull
  public AddSupportStepsProvider[] getAddSupportProviders(ModuleType moduleType) {
    return myStepsProviders.get(moduleType).toArray(AddSupportStepsProvider.EMPTY_ARRAY);
  }

  public ModuleWizardStep[] createAddSupportSteps(WizardContext wizardContext,
                                                  ModuleBuilder moduleBuilder,
                                                  ModulesProvider modulesProvider,
                                                  final Icon icon) {

    ArrayList<ModuleWizardStep> result = new ArrayList<ModuleWizardStep>();
    ArrayList<AddSupportContext> contexts = new ArrayList<AddSupportContext>();
    final AddSupportStepsProvider[] providers = ProjectWizardStepFactory.getInstance().getAddSupportProviders(moduleBuilder.getModuleType());
    if (providers.length > 0) {
      for (AddSupportStepsProvider provider: providers) {
        final AddSupportStep[] wizardSteps = provider.createAddSupportSteps(wizardContext, moduleBuilder, modulesProvider);
        result.addAll(Arrays.asList(wizardSteps));
        contexts.add(wizardSteps[0].myContext);
      }
      final AddSupportContext[] supportContexts = contexts.toArray(new AddSupportContext[contexts.size()]);
      final AddSupportFeaturesStep featuresStep =
        new AddSupportFeaturesStep(providers, supportContexts, icon);
      result.add(0, featuresStep);
    }
    return result.toArray(ModuleWizardStep.EMPTY_ARRAY);
  }

  public ModuleWizardStep createProjectJdkStep(final WizardContext wizardContext) {
    return new ProjectJdkStep(wizardContext){
      public boolean isStepVisible() {
        return com.intellij.ide.util.newProjectWizard.AddModuleWizard.getNewProjectJdk(wizardContext) == null;
      }
    };
  }

}