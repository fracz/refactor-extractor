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

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 22.11.2006
 * Time: 19:59:36
 */
package com.intellij.openapi.vcs.changes.shelf;

import com.intellij.lifecycle.PeriodicalTasksCloser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.*;
import com.intellij.openapi.diff.impl.patch.apply.ApplyFilePatchBase;
import com.intellij.openapi.diff.impl.patch.formove.CustomBinaryPatchApplier;
import com.intellij.openapi.diff.impl.patch.formove.PatchApplier;
import com.intellij.openapi.options.BaseSchemeProcessor;
import com.intellij.openapi.options.SchemesManager;
import com.intellij.openapi.options.SchemesManagerFactory;
import com.intellij.openapi.progress.AsynchronousExecution;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.patch.ApplyPatchDefaultExecutor;
import com.intellij.openapi.vcs.changes.patch.PatchFileType;
import com.intellij.openapi.vcs.changes.patch.PatchNameChecker;
import com.intellij.openapi.vcs.changes.ui.RollbackChangesDialog;
import com.intellij.openapi.vcs.changes.ui.RollbackWorker;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.continuation.*;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import com.intellij.util.text.CharArrayCharSequence;
import com.intellij.util.text.UniqueNameGenerator;
import com.intellij.util.ui.UIUtil;
import com.intellij.vcsUtil.FilesProgress;
import org.jdom.Element;
import org.jdom.Parent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.*;

public class ShelveChangesManager extends AbstractProjectComponent implements JDOMExternalizable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager");
  @NonNls private static final String ELEMENT_CHANGELIST = "changelist";
  @NonNls private static final String ELEMENT_RECYCLED_CHANGELIST = "recycled_changelist";

  @NotNull private final SchemesManager<ShelvedChangeList, ShelvedChangeList> mySchemeManager;

  public static ShelveChangesManager getInstance(Project project) {
    return PeriodicalTasksCloser.getInstance().safeGetComponent(project, ShelveChangesManager.class);
  }

  private static final String SHELVE_MANAGER_DIR_PATH = "shelf";
  private final MessageBus myBus;

  @NonNls private static final String ATTRIBUTE_SHOW_RECYCLED = "show_recycled";
  @NotNull private final CompoundShelfFileProcessor myFileProcessor;

  public static final Topic<ChangeListener> SHELF_TOPIC = new Topic<ChangeListener>("shelf updates", ChangeListener.class);
  private boolean myShowRecycled;

  public ShelveChangesManager(final Project project, final MessageBus bus) {
    super(project);

    myBus = bus;
    mySchemeManager =
      SchemesManagerFactory.getInstance(project).create(SHELVE_MANAGER_DIR_PATH, new BaseSchemeProcessor<ShelvedChangeList>() {
        @Nullable
        @Override
        public ShelvedChangeList readScheme(@NotNull Element element) throws InvalidDataException {
          return readOneShelvedChangeList(element);
        }

        @Override
        public Parent writeScheme(@NotNull ShelvedChangeList scheme) throws WriteExternalException {
          Element child = new Element(ELEMENT_CHANGELIST);
          scheme.writeExternal(child);
          return child;
        }
      });
    myFileProcessor = new CompoundShelfFileProcessor(mySchemeManager.getRootDirectory());
    ChangeListManager.getInstance(project).addDirectoryToIgnoreImplicitly(mySchemeManager.getRootDirectory().getAbsolutePath());
    mySchemeManager.loadSchemes();
  }

  @NotNull
  private static ShelvedChangeList readOneShelvedChangeList(@NotNull Element element) throws InvalidDataException {
    ShelvedChangeList data = new ShelvedChangeList();
    data.readExternal(element);
    return data;
  }

  @Override
  @NonNls
  @NotNull
  public String getComponentName() {
    return "ShelveChangesManager";
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    final String showRecycled = element.getAttributeValue(ATTRIBUTE_SHOW_RECYCLED);
    if (showRecycled != null) {
      myShowRecycled = Boolean.parseBoolean(showRecycled);
    }
    else {
      myShowRecycled = true;
    }
    migrateOldShelfInfo(element, true);
    migrateOldShelfInfo(element, false);
  }

  // could be very long; runs on Pool Thread
  private void migrateOldShelfInfo(@NotNull Element element, boolean recycled) throws InvalidDataException {
    for (Element changeSetElement : element.getChildren(recycled ? ELEMENT_RECYCLED_CHANGELIST : ELEMENT_CHANGELIST)) {
      ShelvedChangeList list = readOneShelvedChangeList(changeSetElement);
      final File oldPatchFile = new File(list.PATH);
      if (oldPatchFile.exists()) {
        list.setRecycled(recycled);
        File newPatchDir = generateUniqueSchemePatchDir(list.DESCRIPTION);
        File newPatchFile = getPatchFileInConfigDir(list.DESCRIPTION, newPatchDir);
        try {
          FileUtil.copy(oldPatchFile, newPatchFile);
          list.PATH = newPatchFile.getPath();
          for (ShelvedBinaryFile file : list.getBinaryFiles()) {
            if (file.SHELVED_PATH != null) {
              File shelvedFile = new File(file.SHELVED_PATH);

              if (!StringUtil.isEmptyOrSpaces(file.AFTER_PATH) && shelvedFile.exists()) {
                File newShelvedFile = new File(newPatchDir, PathUtil.getFileName(file.AFTER_PATH));
                FileUtil.copy(shelvedFile, newShelvedFile);
                file.SHELVED_PATH = newShelvedFile.getPath();
                FileUtil.delete(shelvedFile);
              }
            }
          }
          FileUtil.delete(oldPatchFile);
          list.setName(newPatchDir.getName());
        }
        catch (IOException e) {
          LOG.error("Couldn't migrate shelf resources information from " + list.PATH);
          continue;
        }
        mySchemeManager.addNewScheme(list, false);
      }
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(ATTRIBUTE_SHOW_RECYCLED, Boolean.toString(myShowRecycled));
  }

  public List<ShelvedChangeList> getShelvedChangeLists() {
    return getRecycled(false);
  }

  @NotNull
  private List<ShelvedChangeList> getRecycled(final boolean recycled) {
    return ContainerUtil.newUnmodifiableList(ContainerUtil.filter(mySchemeManager.getAllSchemes(), new Condition<ShelvedChangeList>() {
      @Override
      public boolean value(ShelvedChangeList list) {
        return recycled ? list.isRecycled() : !list.isRecycled();
      }
    }));
  }

  public ShelvedChangeList shelveChanges(final Collection<Change> changes, final String commitMessage, final boolean rollback)
    throws IOException, VcsException {
    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    if (progressIndicator != null) {
      progressIndicator.setText(VcsBundle.message("shelve.changes.progress.title"));
    }
    File schemePatchDir = generateUniqueSchemePatchDir(commitMessage);
    final List<Change> textChanges = new ArrayList<Change>();
    final List<ShelvedBinaryFile> binaryFiles = new ArrayList<ShelvedBinaryFile>();
    for (Change change : changes) {
      if (ChangesUtil.getFilePath(change).isDirectory()) {
        continue;
      }
      if (change.getBeforeRevision() instanceof BinaryContentRevision || change.getAfterRevision() instanceof BinaryContentRevision) {
        binaryFiles.add(shelveBinaryFile(schemePatchDir, change));
      }
      else {
        textChanges.add(change);
      }
    }

    final ShelvedChangeList changeList;
    try {
      File patchPath = getPatchFileInConfigDir(commitMessage, schemePatchDir);
      ProgressManager.checkCanceled();
      final List<FilePatch> patches =
        IdeaTextPatchBuilder.buildPatch(myProject, textChanges, myProject.getBaseDir().getPresentableUrl(), false);
      ProgressManager.checkCanceled();

      CommitContext commitContext = new CommitContext();
      baseRevisionsOfDvcsIntoContext(textChanges, commitContext);
      myFileProcessor.savePathFile(
        new CompoundShelfFileProcessor.ContentProvider() {
          @Override
          public void writeContentTo(final Writer writer, CommitContext commitContext) throws IOException {
            UnifiedDiffWriter.write(myProject, patches, writer, "\n", commitContext);
          }
        },
        patchPath, commitContext);

      changeList = new ShelvedChangeList(patchPath.toString(), commitMessage.replace('\n', ' '), binaryFiles);
      changeList.setName(schemePatchDir.getName());
      ProgressManager.checkCanceled();
      mySchemeManager.addNewScheme(changeList, false);

      if (rollback) {
        final String operationName = UIUtil.removeMnemonic(RollbackChangesDialog.operationNameByChanges(myProject, changes));
        boolean modalContext = ApplicationManager.getApplication().isDispatchThread() && LaterInvocator.isInModalContext();
        if (progressIndicator != null) {
          progressIndicator.startNonCancelableSection();
        }
        new RollbackWorker(myProject, operationName, modalContext).
          doRollback(changes, true, null, VcsBundle.message("shelve.changes.action"));
      }
    }
    finally {
      notifyStateChanged();
    }

    return changeList;
  }

  @NotNull
  private static File getPatchFileInConfigDir(String commitMessage, File schemePatchDir) {
    return new File(schemePatchDir, commitMessage + "." + VcsConfiguration.PATCH);
  }

  private void baseRevisionsOfDvcsIntoContext(List<Change> textChanges, CommitContext commitContext) {
    ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    if (vcsManager.dvcsUsedInProject() && VcsConfiguration.getInstance(myProject).INCLUDE_TEXT_INTO_SHELF) {
      final Set<Change> big = SelectFilesToAddTextsToPatchPanel.getBig(textChanges);
      final ArrayList<FilePath> toKeep = new ArrayList<FilePath>();
      for (Change change : textChanges) {
        if (change.getBeforeRevision() == null || change.getAfterRevision() == null) continue;
        if (big.contains(change)) continue;
        FilePath filePath = ChangesUtil.getFilePath(change);
        final AbstractVcs vcs = vcsManager.getVcsFor(filePath);
        if (vcs != null && VcsType.distributed.equals(vcs.getType())) {
          toKeep.add(filePath);
        }
      }
      commitContext.putUserData(BaseRevisionTextPatchEP.ourPutBaseRevisionTextKey, true);
      commitContext.putUserData(BaseRevisionTextPatchEP.ourBaseRevisionPaths, toKeep);
    }
  }

  public ShelvedChangeList importFilePatches(final String fileName, final List<FilePatch> patches, final PatchEP[] patchTransitExtensions)
    throws IOException {
    try {
      File schemePatchDir = generateUniqueSchemePatchDir(fileName);
      File patchPath = getPatchFileInConfigDir(fileName, schemePatchDir);
      myFileProcessor.savePathFile(
        new CompoundShelfFileProcessor.ContentProvider() {
          @Override
          public void writeContentTo(final Writer writer, CommitContext commitContext) throws IOException {
            UnifiedDiffWriter.write(myProject, patches, writer, "\n", patchTransitExtensions, commitContext);
          }
        },
        patchPath, new CommitContext());

      final ShelvedChangeList changeList =
        new ShelvedChangeList(patchPath.toString(), fileName.replace('\n', ' '), new SmartList<ShelvedBinaryFile>());
      changeList.setName(schemePatchDir.getName());
      mySchemeManager.addNewScheme(changeList, false);
      return changeList;
    }
    finally {
      notifyStateChanged();
    }
  }

  public List<VirtualFile> gatherPatchFiles(final Collection<VirtualFile> files) {
    final List<VirtualFile> result = new ArrayList<VirtualFile>();

    final LinkedList<VirtualFile> filesQueue = new LinkedList<VirtualFile>(files);
    while (!filesQueue.isEmpty()) {
      ProgressManager.checkCanceled();
      final VirtualFile file = filesQueue.removeFirst();
      if (file.isDirectory()) {
        filesQueue.addAll(Arrays.asList(file.getChildren()));
        continue;
      }
      if (PatchFileType.NAME.equals(file.getFileType().getName())) {
        result.add(file);
      }
    }

    return result;
  }

  public List<ShelvedChangeList> importChangeLists(final Collection<VirtualFile> files,
                                                   final Consumer<VcsException> exceptionConsumer) {
    final List<ShelvedChangeList> result = new ArrayList<ShelvedChangeList>(files.size());
    try {
      final FilesProgress filesProgress = new FilesProgress(files.size(), "Processing ");
      for (VirtualFile file : files) {
        filesProgress.updateIndicator(file);
        final String description = file.getNameWithoutExtension().replace('_', ' ');
        File schemeDir = generateUniqueSchemePatchDir(description);
        final File patchPath = getPatchFileInConfigDir(description, schemeDir);
        final ShelvedChangeList list = new ShelvedChangeList(patchPath.getPath(), description, new SmartList<ShelvedBinaryFile>(),
                                                             file.getTimeStamp());
        list.setName(patchPath.getName());
        try {
          final List<TextFilePatch> patchesList = loadPatches(myProject, file.getPath(), new CommitContext());
          if (!patchesList.isEmpty()) {
            FileUtil.copy(new File(file.getPath()), patchPath);
            // add only if ok to read patch
            mySchemeManager.addNewScheme(list, false);
            result.add(list);
          }
        }
        catch (IOException e) {
          exceptionConsumer.consume(new VcsException(e));
        }
        catch (PatchSyntaxException e) {
          exceptionConsumer.consume(new VcsException(e));
        }
      }
    }
    finally {
      notifyStateChanged();
    }
    return result;
  }

  private ShelvedBinaryFile shelveBinaryFile(@NotNull File schemePatchDir, final Change change) throws IOException {
    final ContentRevision beforeRevision = change.getBeforeRevision();
    final ContentRevision afterRevision = change.getAfterRevision();
    File beforeFile = beforeRevision == null ? null : beforeRevision.getFile().getIOFile();
    File afterFile = afterRevision == null ? null : afterRevision.getFile().getIOFile();
    String shelvedPath = null;
    if (afterFile != null) {
      File shelvedFile = new File(schemePatchDir, afterFile.getName());
      FileUtil.copy(afterRevision.getFile().getIOFile(), shelvedFile);
      shelvedPath = shelvedFile.getPath();
    }
    String beforePath = ChangesUtil.getProjectRelativePath(myProject, beforeFile);
    String afterPath = ChangesUtil.getProjectRelativePath(myProject, afterFile);
    return new ShelvedBinaryFile(beforePath, afterPath, shelvedPath);
  }

  private void notifyStateChanged() {
    if (!myProject.isDisposed()) {
      myBus.syncPublisher(SHELF_TOPIC).stateChanged(new ChangeEvent(this));
    }
  }

  private File generateUniqueSchemePatchDir(final String defaultName) {
    String[] children = mySchemeManager.getRootDirectory().list();
    String uniqueName = UniqueNameGenerator
      .generateUniqueName(FileUtil.sanitizeName(defaultName),
                          children != null ? ContainerUtil.newArrayList(children) : ContainerUtil.<String>emptyList());
    File dir = new File(myFileProcessor.getBaseDir(), uniqueName);
    if (!dir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      dir.mkdirs();
    }
    return dir;
  }

  // for create patch only; todo move or unify with unique directory creation
  public static File suggestPatchName(Project project, final String commitMessage, final File file, String extension) {
    @NonNls String defaultPath = PathUtil.suggestFileName(commitMessage);
    if (defaultPath.isEmpty()) {
      defaultPath = "unnamed";
    }
    if (defaultPath.length() > PatchNameChecker.MAX - 10) {
      defaultPath = defaultPath.substring(0, PatchNameChecker.MAX - 10);
    }
    while (true) {
      final File nonexistentFile = FileUtil.findSequentNonexistentFile(file, defaultPath,
                                                                       extension == null
                                                                       ? VcsConfiguration.getInstance(project).getPatchFileExtension()
                                                                       : extension);
      if (nonexistentFile.getName().length() >= PatchNameChecker.MAX) {
        defaultPath = defaultPath.substring(0, defaultPath.length() - 1);
        continue;
      }
      return nonexistentFile;
    }
  }

  public void unshelveChangeList(@NotNull final ShelvedChangeList changeList, @Nullable final List<ShelvedChange> changes,
                                 @Nullable final List<ShelvedBinaryFile> binaryFiles, final LocalChangeList targetChangeList) {
    unshelveChangeList(changeList, changes, binaryFiles, targetChangeList, true);
  }

  @AsynchronousExecution
  private void unshelveChangeList(@NotNull final ShelvedChangeList changeList,
                                  @Nullable final List<ShelvedChange> changes,
                                  @Nullable final List<ShelvedBinaryFile> binaryFiles,
                                  @Nullable final LocalChangeList targetChangeList,
                                  boolean showSuccessNotification) {
    final Continuation continuation = Continuation.createForCurrentProgress(myProject, true, "Unshelve changes");
    final GatheringContinuationContext initContext = new GatheringContinuationContext();
    scheduleUnshelveChangeList(changeList, changes, binaryFiles, targetChangeList, showSuccessNotification, initContext, false,
                               false, null, null);
    continuation.run(initContext.getList());
  }

  @AsynchronousExecution
  public void scheduleUnshelveChangeList(@NotNull final ShelvedChangeList changeList,
                                         @Nullable final List<ShelvedChange> changes,
                                         @Nullable final List<ShelvedBinaryFile> binaryFiles,
                                         @Nullable final LocalChangeList targetChangeList,
                                         final boolean showSuccessNotification,
                                         final ContinuationContext context,
                                         final boolean systemOperation,
                                         final boolean reverse,
                                         final String leftConflictTitle,
                                         final String rightConflictTitle) {
    context.next(new TaskDescriptor("", Where.AWT) {
      @Override
      public void run(ContinuationContext contextInner) {
        final List<FilePatch> remainingPatches = new ArrayList<FilePatch>();

        final CommitContext commitContext = new CommitContext();
        final List<TextFilePatch> textFilePatches;
        try {
          textFilePatches = loadTextPatches(myProject, changeList, changes, remainingPatches, commitContext);
        }
        catch (IOException e) {
          LOG.info(e);
          PatchApplier.showError(myProject, "Cannot load patch(es): " + e.getMessage(), true);
          return;
        }
        catch (PatchSyntaxException e) {
          PatchApplier.showError(myProject, "Cannot load patch(es): " + e.getMessage(), true);
          LOG.info(e);
          return;
        }

        final List<FilePatch> patches = new ArrayList<FilePatch>(textFilePatches);

        final List<ShelvedBinaryFile> remainingBinaries = new ArrayList<ShelvedBinaryFile>();
        final List<ShelvedBinaryFile> binaryFilesToUnshelve = getBinaryFilesToUnshelve(changeList, binaryFiles, remainingBinaries);

        for (final ShelvedBinaryFile shelvedBinaryFile : binaryFilesToUnshelve) {
          patches.add(new ShelvedBinaryFilePatch(shelvedBinaryFile));
        }

        final BinaryPatchApplier binaryPatchApplier = new BinaryPatchApplier();
        final PatchApplier<ShelvedBinaryFilePatch> patchApplier =
          new PatchApplier<ShelvedBinaryFilePatch>(myProject, myProject.getBaseDir(),
                                                   patches, targetChangeList, binaryPatchApplier, commitContext, reverse, leftConflictTitle,
                                                   rightConflictTitle);
        patchApplier.setIsSystemOperation(systemOperation);

        // after patch applier part
        contextInner.next(new TaskDescriptor("", Where.AWT) {
          @Override
          public void run(ContinuationContext context) {
            remainingPatches.addAll(patchApplier.getRemainingPatches());

            if (remainingPatches.isEmpty() && remainingBinaries.isEmpty()) {
              recycleChangeList(changeList);
            }
            else {
              saveRemainingPatches(changeList, remainingPatches, remainingBinaries, commitContext);
            }
          }
        });

        patchApplier.scheduleSelf(showSuccessNotification, contextInner, systemOperation);
      }
    });
  }

  private static List<TextFilePatch> loadTextPatches(final Project project,
                                                     final ShelvedChangeList changeList,
                                                     final List<ShelvedChange> changes,
                                                     final List<FilePatch> remainingPatches,
                                                     final CommitContext commitContext)
    throws IOException, PatchSyntaxException {
    final List<TextFilePatch> textFilePatches = loadPatches(project, changeList.PATH, commitContext);

    if (changes != null) {
      final Iterator<TextFilePatch> iterator = textFilePatches.iterator();
      while (iterator.hasNext()) {
        TextFilePatch patch = iterator.next();
        if (!needUnshelve(patch, changes)) {
          remainingPatches.add(patch);
          iterator.remove();
        }
      }
    }
    return textFilePatches;
  }

  private class BinaryPatchApplier implements CustomBinaryPatchApplier<ShelvedBinaryFilePatch> {
    private final List<FilePatch> myAppliedPatches;

    private BinaryPatchApplier() {
      myAppliedPatches = new ArrayList<FilePatch>();
    }

    @Override
    @NotNull
    public ApplyPatchStatus apply(final List<Pair<VirtualFile, ApplyFilePatchBase<ShelvedBinaryFilePatch>>> patches) throws IOException {
      for (Pair<VirtualFile, ApplyFilePatchBase<ShelvedBinaryFilePatch>> patch : patches) {
        final ShelvedBinaryFilePatch shelvedPatch = patch.getSecond().getPatch();
        unshelveBinaryFile(shelvedPatch.getShelvedBinaryFile(), patch.getFirst());
        myAppliedPatches.add(shelvedPatch);
      }
      return ApplyPatchStatus.SUCCESS;
    }

    @Override
    @NotNull
    public List<FilePatch> getAppliedPatches() {
      return myAppliedPatches;
    }
  }

  private static List<ShelvedBinaryFile> getBinaryFilesToUnshelve(final ShelvedChangeList changeList,
                                                                  final List<ShelvedBinaryFile> binaryFiles,
                                                                  final List<ShelvedBinaryFile> remainingBinaries) {
    if (binaryFiles == null) {
      return new ArrayList<ShelvedBinaryFile>(changeList.getBinaryFiles());
    }
    ArrayList<ShelvedBinaryFile> result = new ArrayList<ShelvedBinaryFile>();
    for (ShelvedBinaryFile file : changeList.getBinaryFiles()) {
      if (binaryFiles.contains(file)) {
        result.add(file);
      }
      else {
        remainingBinaries.add(file);
      }
    }
    return result;
  }

  private void unshelveBinaryFile(final ShelvedBinaryFile file, @NotNull final VirtualFile patchTarget) throws IOException {
    final Ref<IOException> ex = new Ref<IOException>();
    final Ref<VirtualFile> patchedFileRef = new Ref<VirtualFile>();
    final File shelvedFile = file.SHELVED_PATH == null ? null : new File(file.SHELVED_PATH);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        try {
          if (shelvedFile == null) {
            patchTarget.delete(this);
          }
          else {
            patchTarget.setBinaryContent(FileUtil.loadFileBytes(shelvedFile));
            patchedFileRef.set(patchTarget);
          }
        }
        catch (IOException e) {
          ex.set(e);
        }
      }
    });
    if (!ex.isNull()) {
      throw ex.get();
    }
  }

  private static boolean needUnshelve(final FilePatch patch, final List<ShelvedChange> changes) {
    for (ShelvedChange change : changes) {
      if (Comparing.equal(patch.getBeforeName(), change.getBeforePath())) {
        return true;
      }
    }
    return false;
  }

  private static void writePatchesToFile(final Project project,
                                         final String path,
                                         final List<FilePatch> remainingPatches,
                                         CommitContext commitContext) {
    try {
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), CharsetToolkit.UTF8_CHARSET);
      try {
        UnifiedDiffWriter.write(project, remainingPatches, writer, "\n", commitContext);
      }
      finally {
        writer.close();
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  void saveRemainingPatches(final ShelvedChangeList changeList, final List<FilePatch> remainingPatches,
                            final List<ShelvedBinaryFile> remainingBinaries, CommitContext commitContext) {
    final File newPatchDir = generateUniqueSchemePatchDir(changeList.DESCRIPTION);
    final File newPath = getPatchFileInConfigDir(changeList.DESCRIPTION, newPatchDir);
    try {
      FileUtil.copy(new File(changeList.PATH), newPath);
    }
    catch (IOException e) {
      // do not delete if cannot recycle
      return;
    }
    final ShelvedChangeList listCopy = new ShelvedChangeList(newPath.getAbsolutePath(), changeList.DESCRIPTION,
                                                             new ArrayList<ShelvedBinaryFile>(changeList.getBinaryFiles()));
    listCopy.setName(newPatchDir.getName());
    listCopy.DATE = changeList.DATE == null ? null : new Date(changeList.DATE.getTime());

    writePatchesToFile(myProject, changeList.PATH, remainingPatches, commitContext);

    changeList.getBinaryFiles().retainAll(remainingBinaries);
    changeList.clearLoadedChanges();
    recycleChangeList(listCopy, changeList);
    // all newly create ShelvedChangeList have to be added to SchemesManger as new scheme
    mySchemeManager.addNewScheme(listCopy, false);
    notifyStateChanged();
  }

  public void restoreList(@NotNull final ShelvedChangeList changeList) {
    ShelvedChangeList list = mySchemeManager.findSchemeByName(changeList.getName());
    if (list != null) {
      list.setRecycled(false);
    }
    notifyStateChanged();
  }

  public List<ShelvedChangeList> getRecycledShelvedChangeLists() {
    return getRecycled(true);
  }

  public void clearRecycled() {
    for (ShelvedChangeList list : getRecycledShelvedChangeLists()) {
      deleteListImpl(list);
      mySchemeManager.removeScheme(list);
    }
    notifyStateChanged();
  }

  private void recycleChangeList(@NotNull final ShelvedChangeList listCopy, @Nullable final ShelvedChangeList newList) {
    if (newList != null) {
      for (Iterator<ShelvedBinaryFile> shelvedChangeListIterator = listCopy.getBinaryFiles().iterator();
           shelvedChangeListIterator.hasNext(); ) {
        final ShelvedBinaryFile binaryFile = shelvedChangeListIterator.next();
        for (ShelvedBinaryFile newBinary : newList.getBinaryFiles()) {
          if (Comparing.equal(newBinary.BEFORE_PATH, binaryFile.BEFORE_PATH)
              && Comparing.equal(newBinary.AFTER_PATH, binaryFile.AFTER_PATH)) {
            shelvedChangeListIterator.remove();
          }
        }
      }
      for (Iterator<ShelvedChange> iterator = listCopy.getChanges(myProject).iterator(); iterator.hasNext(); ) {
        final ShelvedChange change = iterator.next();
        for (ShelvedChange newChange : newList.getChanges(myProject)) {
          if (Comparing.equal(change.getBeforePath(), newChange.getBeforePath()) &&
              Comparing.equal(change.getAfterPath(), newChange.getAfterPath())) {
            iterator.remove();
          }
        }
      }

      // needed only if partial unshelve
      try {
        final CommitContext commitContext = new CommitContext();
        final List<FilePatch> patches = new ArrayList<FilePatch>();
        for (ShelvedChange change : listCopy.getChanges(myProject)) {
          patches.add(change.loadFilePatch(myProject, commitContext));
        }
        writePatchesToFile(myProject, listCopy.PATH, patches, commitContext);
      }
      catch (IOException e) {
        LOG.info(e);
        // left file as is
      }
      catch (PatchSyntaxException e) {
        LOG.info(e);
        // left file as is
      }
    }

    if (!listCopy.getBinaryFiles().isEmpty() || !listCopy.getChanges(myProject).isEmpty()) {
      listCopy.setRecycled(true);
      notifyStateChanged();
    }
  }

  private void recycleChangeList(@NotNull final ShelvedChangeList changeList) {
    recycleChangeList(changeList, null);
    notifyStateChanged();
  }

  public void deleteChangeList(@NotNull final ShelvedChangeList changeList) {
    deleteListImpl(changeList);
    mySchemeManager.removeScheme(changeList);
    notifyStateChanged();
  }

  private void deleteListImpl(@NotNull final ShelvedChangeList changeList) {
    FileUtil.delete(new File(myFileProcessor.getBaseDir(), changeList.getName()));
  }

  public void renameChangeList(final ShelvedChangeList changeList, final String newName) {
    changeList.DESCRIPTION = newName;
    notifyStateChanged();
  }

  @NotNull
  public static List<TextFilePatch> loadPatches(Project project,
                                                final String patchPath,
                                                CommitContext commitContext) throws IOException, PatchSyntaxException {
    return loadPatches(project, patchPath, commitContext, true);
  }

  @NotNull
  static List<? extends FilePatch> loadPatchesWithoutContent(Project project,
                                                             final String patchPath,
                                                             CommitContext commitContext) throws IOException, PatchSyntaxException {
    return loadPatches(project, patchPath, commitContext, false);
  }

  private static List<TextFilePatch> loadPatches(Project project,
                                                 final String patchPath,
                                                 CommitContext commitContext,
                                                 boolean loadContent) throws IOException, PatchSyntaxException {
    char[] text = FileUtil.loadFileText(new File(patchPath), CharsetToolkit.UTF8);
    PatchReader reader = new PatchReader(new CharArrayCharSequence(text), loadContent);
    final List<TextFilePatch> textFilePatches = reader.readAllPatches();
    final TransparentlyFailedValueI<Map<String, Map<String, CharSequence>>, PatchSyntaxException> additionalInfo = reader.getAdditionalInfo(
      null);
    ApplyPatchDefaultExecutor.applyAdditionalInfoBefore(project, additionalInfo, commitContext);
    return textFilePatches;
  }

  public static class ShelvedBinaryFilePatch extends FilePatch {
    private final ShelvedBinaryFile myShelvedBinaryFile;

    public ShelvedBinaryFilePatch(final ShelvedBinaryFile shelvedBinaryFile) {
      myShelvedBinaryFile = shelvedBinaryFile;
      setBeforeName(myShelvedBinaryFile.BEFORE_PATH);
      setAfterName(myShelvedBinaryFile.AFTER_PATH);
    }

    public static ShelvedBinaryFilePatch patchCopy(@NotNull final ShelvedBinaryFilePatch patch) {
      return new ShelvedBinaryFilePatch(patch.getShelvedBinaryFile());
    }

    @Override
    public String getBeforeFileName() {
      return getFileName(myShelvedBinaryFile.BEFORE_PATH);
    }

    @Override
    public String getAfterFileName() {
      return getFileName(myShelvedBinaryFile.AFTER_PATH);
    }

    @Nullable
    private static String getFileName(String filePath) {
      return filePath != null ? PathUtil.getFileName(filePath) : null;
    }

    @Override
    public boolean isNewFile() {
      return myShelvedBinaryFile.BEFORE_PATH == null;
    }

    @Override
    public boolean isDeletedFile() {
      return myShelvedBinaryFile.AFTER_PATH == null;
    }

    public ShelvedBinaryFile getShelvedBinaryFile() {
      return myShelvedBinaryFile;
    }
  }

  public boolean isShowRecycled() {
    return myShowRecycled;
  }

  public void setShowRecycled(final boolean showRecycled) {
    myShowRecycled = showRecycled;
    notifyStateChanged();
  }
}