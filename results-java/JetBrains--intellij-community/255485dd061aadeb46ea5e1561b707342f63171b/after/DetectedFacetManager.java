/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.facet.impl.autodetecting;

import com.intellij.ProjectTopics;
import com.intellij.facet.*;
import com.intellij.facet.autodetecting.DetectedFacetPresentation;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.impl.autodetecting.facetsTree.DetectedFacetsDialog;
import com.intellij.facet.impl.autodetecting.model.DetectedFacetInfo;
import com.intellij.facet.impl.autodetecting.model.FacetInfo2;
import com.intellij.facet.impl.autodetecting.model.ProjectFacetInfoSet;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.MultiValuesMap;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author nik
 */
public class DetectedFacetManager implements Disposable {
  @NonNls private static final String NOTIFICATION_ID = "Facet Detector";
  private static final int NOTIFICATION_DELAY = 200;
  private final Project myProject;
  private final FacetAutodetectingManagerImpl myAutodetectingManager;
  private final ProjectWideFacetListenersRegistry myProjectWideFacetListenersRegistry;
  private boolean myUIInitialized;
  private final Set<DetectedFacetInfo<Module>> myPendingNewFacets = new HashSet<DetectedFacetInfo<Module>>();
  private final Alarm myNotificationAlarm = new Alarm();
  private final ProjectFacetInfoSet myDetectedFacetSet;

  public DetectedFacetManager(final Project project, final FacetAutodetectingManagerImpl autodetectingManager,
                              final ProjectFacetInfoSet detectedFacetSet) {
    myDetectedFacetSet = detectedFacetSet;
    myProjectWideFacetListenersRegistry = ProjectWideFacetListenersRegistry.getInstance(project);
    myProject = project;
    myAutodetectingManager = autodetectingManager;
    myDetectedFacetSet.addListener(new ProjectFacetInfoSet.DetectedFacetListener() {
      public void facetDetected(final DetectedFacetInfo<Module> info) {
        onDetectedFacetChanged(Collections.singletonList(info), Collections.<DetectedFacetInfo<Module>>emptyList());
      }

      public void facetRemoved(final DetectedFacetInfo<Module> info) {
        myAutodetectingManager.getFileIndex().removeFromIndex(info);
        onDetectedFacetChanged(Collections.<DetectedFacetInfo<Module>>emptyList(), Collections.singletonList(info));
      }
    });
    Disposer.register(myProject, this);
    myProject.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
      @Override
      public void moduleRemoved(final Project project, final Module module) {
        myDetectedFacetSet.removeDetectedFacets(module);
      }
    });
  }

  public <F extends Facet<C>, C extends FacetConfiguration> void registerListeners(final FacetType<F, C> type) {
    myProjectWideFacetListenersRegistry.registerListener(type.getId(), new MyProjectWideFacetListener<F, C>(), this);
  }

  public Project getProject() {
    return myProject;
  }

  public void onDetectedFacetChanged(@NotNull final Collection<DetectedFacetInfo<Module>> added, @NotNull final Collection<DetectedFacetInfo<Module>> removed) {
    if (!myUIInitialized) return;

    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
      final boolean clearNotifications = myDetectedFacetSet.getAllDetectedFacets().isEmpty();

      Runnable runnable = new Runnable() {
        public void run() {
          if (isDisposed()) return;

          myPendingNewFacets.addAll(added);
          if (clearNotifications) {
            //getNotifications().invalidateAll(NOTIFICATION_ID);
          }
          queueNotificationPopup();
        }
      };
      ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL);
    }
  }

  private Notifications getNotifications() {
    return myProject.getMessageBus().syncPublisher(Notifications.TOPIC);
  }

  private void queueNotificationPopup() {
    myNotificationAlarm.cancelAllRequests();
    myNotificationAlarm.addRequest(new Runnable() {
      public void run() {
        firePendingNotifications();
      }
    }, NOTIFICATION_DELAY);
  }

  public boolean isDisposed() {
    return myProject.isDisposed() || !myUIInitialized;
  }

  private void firePendingNotifications() {
    if (myPendingNewFacets.isEmpty() || myProject.isDisposed()) return;

    List<DetectedFacetInfo<Module>> newFacets = new ArrayList<DetectedFacetInfo<Module>>();
    for (DetectedFacetInfo<Module> newFacet : myPendingNewFacets) {
      newFacets.add(newFacet);
    }
    myPendingNewFacets.clear();
    fireNotification(newFacets);
  }

  private void fireNotification(final List<DetectedFacetInfo<Module>> newFacets) {
    if (newFacets.isEmpty()) {
      return;
    }

    boolean showNotification = true;//todo[nik] may be we shouldn't show notification if "Facets Detected" dialog is opened
    HashMap<DetectedFacetInfo<Module>, List<VirtualFile>> filesMap = getFilesMap(newFacets);
    if (!filesMap.isEmpty() && showNotification) {
      String name;
      String description;
      final Set<DetectedFacetInfo<Module>> detectedFacetInfos = filesMap.keySet();
      if (filesMap.size() == 1) {
        final DetectedFacetInfo<Module> facetInfo = detectedFacetInfos.iterator().next();
        name = ProjectBundle.message("notification.name.0.facet.detected", facetInfo.getFacetType().getPresentableName());
        final List<VirtualFile> files = filesMap.get(facetInfo);
        description = getNotificationText(facetInfo, files.toArray(new VirtualFile[files.size()]));
      }
      else {
        name = ProjectBundle.message("notification.name.0.facets.detected", filesMap.size());
        description = "";
      }

      Notifications.Bus.notify(new Notification(NOTIFICATION_ID, name, description , NotificationType.INFORMATION), myProject);
    }
    else {
      myPendingNewFacets.addAll(newFacets);
    }
  }

  private static String getNotificationText(DetectedFacetInfo<Module> detectedFacetInfo, VirtualFile[] files) {
    DetectedFacetPresentation presentation = FacetDetectorRegistryEx.getDetectedFacetPresentation((FacetType<?,? extends FacetConfiguration>)detectedFacetInfo.getFacetType());

    String text = presentation.getAutodetectionPopupText(detectedFacetInfo.getModule(), detectedFacetInfo.getFacetType(),
                                                         detectedFacetInfo.getFacetName(), files);
    if (text == null) {
      text = DefaultDetectedFacetPresentation.INSTANCE.getAutodetectionPopupText(detectedFacetInfo.getModule(), detectedFacetInfo.getFacetType(),
                                                         detectedFacetInfo.getFacetName(), files);
    }
    return ProjectBundle.message("facet.autodetected.info.text", detectedFacetInfo.getFacetType().getPresentableName(),
                                 text, detectedFacetInfo.getFacetName());
  }

  public void dispose() {
  }

  public void initUI() {
    myUIInitialized = true;
    onDetectedFacetChanged(myDetectedFacetSet.getAllDetectedFacets(), Collections.<DetectedFacetInfo<Module>>emptyList());
  }

  public void disposeUI() {
    if (!myUIInitialized) return;

    myNotificationAlarm.cancelAllRequests();
  }

  public void disableDetectionInFile(final DetectedFacetInfo<Module> detectedFacet) {
    Collection<String> urls = myAutodetectingManager.getFileIndex().getFiles(detectedFacet.getId());
    if (urls != null && !urls.isEmpty()) {
      myAutodetectingManager.disableAutodetectionInFiles(detectedFacet.getFacetType(), detectedFacet.getModule(), ArrayUtil.toStringArray(urls));
    }
    myAutodetectingManager.getDetectedFacetSet().removeFacetInfo(detectedFacet);
  }

  public void disableDetectionInModule(final DetectedFacetInfo<Module> detectedFacetInfo) {
    disableDetectionInModule(detectedFacetInfo.getFacetType(), detectedFacetInfo.getModule());
  }

  public void disableDetectionInModule(final FacetType type, final Module module) {
    myAutodetectingManager.disableAutodetectionInModule(type, module);
    myAutodetectingManager.getDetectedFacetSet().removeDetectedFacets(type.getId(), module);
  }

  public void disableDetectionInProject(final DetectedFacetInfo<Module> facetInfo) {
    FacetType facetType = facetInfo.getFacetType();
    myAutodetectingManager.disableAutodetectionInProject(facetType);
    myAutodetectingManager.getDetectedFacetSet().removeDetectedFacets(facetType.getId());
  }

  public void computeImplicitFacetsAndShowDialog() {
    showImplicitFacetsDialog();
  }

  public boolean hasDetectedFacets() {
    return !myDetectedFacetSet.getAllDetectedFacets().isEmpty();
  }

  public boolean showImplicitFacetsDialog() {
    List<DetectedFacetInfo<Module>> detectedFacets = new ArrayList<DetectedFacetInfo<Module>>(myDetectedFacetSet.getAllDetectedFacets());
    HashMap<DetectedFacetInfo<Module>, List<VirtualFile>> filesMap = getFilesMap(detectedFacets);

    if (detectedFacets.isEmpty()) {
      return false;
    }
    DetectedFacetsDialog dialog = new DetectedFacetsDialog(myProject, this, detectedFacets, filesMap);
    dialog.show();
    return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
  }

  private HashMap<DetectedFacetInfo<Module>, List<VirtualFile>> getFilesMap(final List<DetectedFacetInfo<Module>> detectedFacets) {
    MultiValuesMap<DetectedFacetInfo<Module>, DetectedFacetInfo<Module>> facet2Children = new MultiValuesMap<DetectedFacetInfo<Module>, DetectedFacetInfo<Module>>();
    for (DetectedFacetInfo<Module> detected : detectedFacets) {
      FacetInfo2<Module> underlying = detected.getUnderlyingFacetInfo();
      if (underlying instanceof DetectedFacetInfo) {
        facet2Children.put((DetectedFacetInfo<Module>)underlying, detected);
      }
    }

    HashMap<DetectedFacetInfo<Module>, List<VirtualFile>> filesMap = new HashMap<DetectedFacetInfo<Module>, List<VirtualFile>>();
    Set<DetectedFacetInfo<Module>> toRemove = new HashSet<DetectedFacetInfo<Module>>();
    for (DetectedFacetInfo<Module> detected : detectedFacets) {
      Set<String> urls = myAutodetectingManager.getFileIndex().getFiles(detected.getId());
      List<VirtualFile> files = new ArrayList<VirtualFile>();
      if (urls != null) {
        for (String url : urls) {
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
          if (file != null) {
            files.add(file);
          }
        }
      }
      if (!files.isEmpty()) {
        filesMap.put(detected, files);
      }
      else {
        addWithChildren(detected, facet2Children, toRemove);
      }
    }

    detectedFacets.removeAll(toRemove);

    return filesMap;
  }

  private static void addWithChildren(final DetectedFacetInfo<Module> detected,
                               final MultiValuesMap<DetectedFacetInfo<Module>, DetectedFacetInfo<Module>> facet2Children,
                               final Set<DetectedFacetInfo<Module>> result) {
    if (result.add(detected)) {
      Collection<DetectedFacetInfo<Module>> children = facet2Children.get(detected);
      if (children != null) {
        for (DetectedFacetInfo<Module> info : children) {
          addWithChildren(info, facet2Children, result);
        }
      }
    }
  }

  public Facet createFacet(final DetectedFacetInfo<Module> info, final Facet underlyingFacet) {
    final Module module = info.getModule();

    FacetType<?, ?> type = info.getFacetType();
    final Facet facet = createFacet(info, module, underlyingFacet, type);
    ModifiableFacetModel model = FacetManager.getInstance(module).createModifiableModel();
    ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
    final FacetDetector detector = myAutodetectingManager.findDetector(info.getDetectorId());
    if (detector != null) {
      detector.beforeFacetAdded(facet, model, rootModel);
    }
    model.addFacet(facet);
    if (rootModel.isChanged()) {
      rootModel.commit();
    }
    else {
      rootModel.dispose();
    }

    model.commit();
    myAutodetectingManager.getFileIndex().updateIndexEntryForCreatedFacet(info, facet);
    myAutodetectingManager.getDetectedFacetSet().removeFacetInfo(info);
    if (detector != null) {
      detector.afterFacetAdded(facet);
    }
    return facet;
  }

  private static <C extends FacetConfiguration, F extends Facet> Facet createFacet(final DetectedFacetInfo<Module> info, final Module module, final Facet underlyingFacet,
                                                                  final FacetType<F, C> facetType) {
    return FacetManager.getInstance(module).createFacet(facetType, info.getFacetName(), (C)info.getConfiguration(), underlyingFacet);
  }

  private class MyProjectWideFacetListener<F extends Facet<C>, C extends FacetConfiguration> extends ProjectWideFacetAdapter<F> {
    @Override
    public void facetAdded(final F facet) {
      Map<C, FacetInfo2<Module>> map = myDetectedFacetSet.getConfigurations((FacetTypeId<F>)facet.getTypeId(), facet.getModule());
      Collection<FacetInfo2<Module>> infos = map.values();
      Set<VirtualFile> files = new HashSet<VirtualFile>();
      for (FacetInfo2<Module> info : infos) {
        if (info instanceof DetectedFacetInfo) {
          Set<String> urls = myAutodetectingManager.getFileIndex().getFiles(((DetectedFacetInfo)info).getId());
          if (urls != null) {
            for (String url : urls) {
              VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
              if (file != null) {
                files.add(file);
              }
            }
          }
        }
      }
      for (VirtualFile file : files) {
        myAutodetectingManager.processFile(file);
      }
    }

    @Override
    public void beforeFacetRemoved(final F facet) {
      Set<String> files = myAutodetectingManager.getFiles(facet);
      if (files != null) {
        myAutodetectingManager.disableAutodetectionInFiles(facet.getType(), facet.getModule(), ArrayUtil.toStringArray(files));
      }
      myAutodetectingManager.removeFacetFromCache(facet);
    }
  }

  //private class MyNotificationListener implements NotificationListener {
  //  private final Set<DetectedFacetInfo<Module>> myDetectedFacetInfos;
  //
  //  public MyNotificationListener(Set<DetectedFacetInfo<Module>> detectedFacetInfos) {
  //    myDetectedFacetInfos = detectedFacetInfos;
  //  }
  //
  //  @NotNull
  //  public Continue perform() {
  //    if (showImplicitFacetsDialog()) {
  //      return Continue.REMOVE;
  //    }
  //    return Continue.LEAVE;
  //  }
  //
  //  public Continue onRemove() {
  //    return Continue.REMOVE;
  //  }
  //}
}