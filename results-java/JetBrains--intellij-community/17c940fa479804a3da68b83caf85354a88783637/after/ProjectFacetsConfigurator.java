/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.facet.impl;

import com.intellij.facet.*;
import com.intellij.facet.impl.ui.FacetEditor;
import com.intellij.facet.impl.ui.FacetEditorContextBase;
import com.intellij.facet.impl.ui.FacetTreeModel;
import com.intellij.facet.impl.ui.ProjectConfigurableContext;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.util.NotNullFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author nik
 */
public class ProjectFacetsConfigurator implements FacetsProvider, ModuleEditor.ChangeListener {
  private static final Logger LOG = Logger.getInstance("#com.intellij.facet.impl.ProjectFacetsConfigurator");
  private Map<Module, ModifiableFacetModel> myModels = new HashMap<Module, ModifiableFacetModel>();
  private Map<Facet, FacetEditor> myEditors = new HashMap<Facet, FacetEditor>();
  private Map<Module, FacetTreeModel> myTreeModels = new HashMap<Module, FacetTreeModel>();
  private Map<FacetInfo, Facet> myInfo2Facet = new HashMap<FacetInfo, Facet>();
  private Map<Facet, FacetInfo> myFacet2Info = new HashMap<Facet, FacetInfo>();
  private Map<Module, UserDataHolder> mySharedModuleData = new HashMap<Module, UserDataHolder>();
  private Set<Facet> myFacetsToDispose = new HashSet<Facet>();
  private Set<Facet> myChangedFacets = new HashSet<Facet>();
  private Set<Facet> myCreatedFacets = new HashSet<Facet>();
  private final StructureConfigurableContext myContext;
  private final NotNullFunction<Module, ModuleConfigurationState> myModuleStateProvider;
  private UserDataHolderBase myProjectData = new UserDataHolderBase();

  public ProjectFacetsConfigurator(final StructureConfigurableContext context, NotNullFunction<Module, ModuleConfigurationState> moduleStateProvider) {
    myContext = context;
    myModuleStateProvider = moduleStateProvider;
  }

  public void removeFacet(Facet facet) {
    FacetTreeModel treeModel = getTreeModel(facet.getModule());
    FacetInfo facetInfo = myFacet2Info.get(facet);
    if (facetInfo == null) return;

    List<FacetInfo> children = treeModel.getChildren(facetInfo);
    for (FacetInfo child : children) {
      Facet childInfo = myInfo2Facet.get(child);
      if (childInfo != null) {
        removeFacet(childInfo);
      }
    }

    treeModel.removeFacetInfo(facetInfo);
    getOrCreateModifiableModel(facet.getModule()).removeFacet(facet);
    myChangedFacets.remove(facet);
    if (myCreatedFacets.contains(facet)) {
      Disposer.dispose(facet);
    }
    final FacetEditor facetEditor = myEditors.remove(facet);
    if (facetEditor != null) {
      facetEditor.disposeUIResources();
    }
    myFacet2Info.remove(facet);
    myInfo2Facet.remove(facetInfo);
  }

  public Facet createAndAddFacet(Module module, FacetType<?, ?> type, String name, final @Nullable FacetInfo underlyingFacet) {
    final Facet facet = createFacet(type, module, name, myInfo2Facet.get(underlyingFacet));
    myCreatedFacets.add(facet);
    addFacetInfo(facet);
    getOrCreateModifiableModel(module).addFacet(facet);
    return facet;
  }

  public void addFacetInfo(final Facet facet) {
    final FacetInfo exiting = myFacet2Info.get(facet);
    if (exiting != null) {
      LOG.assertTrue(exiting.getName().equals(facet.getName()));
      LOG.assertTrue(exiting.getFacetType().equals(facet.getType()));
      LOG.assertTrue(exiting.getConfiguration().equals(facet.getConfiguration()));
      return;
    }

    FacetInfo info = new FacetInfo(facet.getType(), facet.getName(), facet.getConfiguration(), myFacet2Info.get(facet.getUnderlyingFacet()));
    myFacet2Info.put(facet, info);
    myInfo2Facet.put(info, facet);
    getTreeModel(facet.getModule()).addFacetInfo(info);
  }

  public void addFacetInfos(final Module module) {
    final Facet[] facets = getFacetModel(module).getSortedFacets();
    for (Facet facet : facets) {
      addFacetInfo(facet);
    }
  }

  public void clearMaps() {
    myModels.clear();
    myEditors.clear();
    myTreeModels.clear();
    myInfo2Facet.clear();
    myFacet2Info.clear();
    myChangedFacets.clear();
    mySharedModuleData.clear();
  }

  private static <C extends FacetConfiguration> Facet createFacet(final FacetType<?, C> type, final Module module, String name, final @Nullable Facet underlyingFacet) {
    return FacetManagerImpl.createFacet(type, module, name, type.createDefaultConfiguration(), underlyingFacet);
  }

  private boolean isNewFacet(Facet facet) {
    final ModifiableFacetModel model = myModels.get(facet.getModule());
    return model != null && model.isNewFacet(facet);
  }

  @NotNull
  public ModifiableFacetModel getOrCreateModifiableModel(final Module module) {
    ModifiableFacetModel model = myModels.get(module);
    if (model == null) {
      model = FacetManager.getInstance(module).createModifiableModel();
      model.addListener(new ModifiableFacetModel.Listener() {
        public void onChanged() {
          fireFacetModelChanged(module);
        }
      }, null);
      myModels.put(module, model);
    }
    return model;
  }

  @NotNull
  public FacetEditor getOrCreateEditor(Facet facet) {
    FacetEditor editor = myEditors.get(facet);
    if (editor == null) {
      final Facet underlyingFacet = facet.getUnderlyingFacet();
      final FacetEditorContext parentContext = underlyingFacet != null ? getOrCreateEditor(underlyingFacet).getContext() : null;
      final ModuleConfigurationState state = myModuleStateProvider.fun(facet.getModule());
      final ProjectConfigurableContext context = new MyProjectConfigurableContext(myFacet2Info.get(facet), facet, parentContext, state);
      editor = new FacetEditor(context, facet.getConfiguration());
      editor.getComponent();
      editor.reset();
      myEditors.put(facet, editor);
    }
    return editor;
  }

  private UserDataHolder getSharedModuleData(final Module module) {
    UserDataHolder dataHolder = mySharedModuleData.get(module);
    if (dataHolder == null) {
      dataHolder = new UserDataHolderBase();
      mySharedModuleData.put(module, dataHolder);
    }
    return dataHolder;
  }

  @NotNull
  public FacetModel getFacetModel(Module module) {
    final ModifiableFacetModel model = myModels.get(module);
    if (model != null) {
      return model;
    }
    return FacetManager.getInstance(module);
  }

  public void commitFacets() {
    for (ModifiableFacetModel model : myModels.values()) {
      model.commit();
    }

    for (Map.Entry<Facet, FacetEditor> entry : myEditors.entrySet()) {
      entry.getValue().onFacetAdded(entry.getKey());
    }

    myModels.clear();
    for (Facet facet : myChangedFacets) {
      facet.getModule().getMessageBus().syncPublisher(FacetManager.FACETS_TOPIC).facetConfigurationChanged(facet);
    }
    myChangedFacets.clear();
  }

  public void resetEditors() {
    for (FacetEditor editor : myEditors.values()) {
      editor.reset();
    }
  }

  public void applyEditors() throws ConfigurationException {
    for (Map.Entry<Facet,FacetEditor> entry : myEditors.entrySet()) {
      final FacetEditor editor = entry.getValue();
      if (editor.isModified()) {
        myChangedFacets.add(entry.getKey());
      }
      editor.apply();
    }
  }

  public boolean isModified() {
    for (ModifiableFacetModel model : myModels.values()) {
      if (model.isModified()) {
        return true;
      }
    }
    for (FacetEditor editor : myEditors.values()) {
      if (editor.isModified()) {
        return true;
      }
    }
    return false;
  }

  public FacetTreeModel getTreeModel(Module module) {
    FacetTreeModel treeModel = myTreeModels.get(module);
    if (treeModel == null) {
      treeModel = new FacetTreeModel();
      myTreeModels.put(module, treeModel);
    }
    return treeModel;
  }

  public FacetInfo getFacetInfo(final Facet facet) {
    return myFacet2Info.get(facet);
  }

  public Facet getFacet(final FacetInfo facetInfo) {
    return myInfo2Facet.get(facetInfo);
  }

  public void disposeEditors() {
    for (Facet facet : myFacetsToDispose) {
      Disposer.dispose(facet);
    }
    myFacetsToDispose.clear();
    myCreatedFacets.clear();

    for (FacetEditor editor : myEditors.values()) {
      editor.disposeUIResources();
    }
    myProjectData = null;
  }

  @NotNull
  public Facet[] getAllFacets(final Module module) {
    return getFacetModel(module).getAllFacets();
  }

  @NotNull
  public <F extends Facet> Collection<F> getFacetsByType(final Module module, final FacetTypeId<F> type) {
    return getFacetModel(module).getFacetsByType(type);
  }

  @Nullable
  public <F extends Facet> F findFacet(final Module module, final FacetTypeId<F> type, final String name) {
    return getFacetModel(module).findFacet(type, name);
  }

  public void moduleStateChanged(final ModifiableRootModel moduleRootModel) {
    Module module = moduleRootModel.getModule();
    Facet[] allFacets = getAllFacets(module);
    for (Facet facet : allFacets) {
      FacetEditor facetEditor = myEditors.get(facet);
      if (facetEditor != null) {
        ((FacetEditorContextBase)facetEditor.getContext()).fireModuleRootsChanged(moduleRootModel);
      }
    }
  }

  private void fireFacetModelChanged(Module module) {
    for (Facet facet : getAllFacets(module)) {
      FacetEditor facetEditor = myEditors.get(facet);
      if (facetEditor != null) {
        ((FacetEditorContextBase)facetEditor.getContext()).fireFacetModelChanged(module);
      }
    }
  }

  private UserDataHolder getProjectData() {
    if (myProjectData == null) {
      myProjectData = new UserDataHolderBase();
    }
    return myProjectData;
  }

  public void onModuleRemoved(final Module module) {
    FacetModel facetModel = getFacetModel(module);
    for (Facet facet : facetModel.getAllFacets()) {
      if (!myCreatedFacets.contains(facet)) {
        myFacetsToDispose.add(facet);
      }
      removeFacet(facet);
    }
    mySharedModuleData.remove(module);
    myModels.remove(module);
  }

  private class MyProjectConfigurableContext extends ProjectConfigurableContext {
    private LibrariesContainer myContainer;

    public MyProjectConfigurableContext(FacetInfo facetInfo, final Facet facet, final FacetEditorContext parentContext, final ModuleConfigurationState state) {
      super(facetInfo, facet, ProjectFacetsConfigurator.this.isNewFacet(facet), parentContext, state,
            ProjectFacetsConfigurator.this.getSharedModuleData(facet.getModule()), getProjectData());
      myContainer = LibrariesContainerFactory.createContainer(facet.getModule().getProject(), myContext);
    }

    public LibrariesContainer getContainer() {
      return myContainer;
    }

  }
}