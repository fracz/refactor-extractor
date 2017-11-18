package com.intellij.openapi.vcs.changes.committed;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author yole
 */
public class CommittedChangesCache {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.committed.CommittedChangesCache");
  @NonNls private static final String VCS_CACHE_PATH = "vcsCache";
  private Map<RepositoryLocation, ChangesCacheFile> myCacheFiles = new HashMap<RepositoryLocation, ChangesCacheFile>();
  private int myInitialCount = 500;
  private MessageBus myBus;
  private BackgroundTaskQueue myTaskQueue;
  private boolean myRefreshingIncomingChanges = false;

  public static final Topic<CommittedChangesListener> COMMITTED_TOPIC = new Topic<CommittedChangesListener>("committed changes updates",
                                                                                                            CommittedChangesListener.class);

  public static CommittedChangesCache getInstance(Project project) {
    return ServiceManager.getService(project, CommittedChangesCache.class);
  }

  private Project myProject;

  public CommittedChangesCache(final Project project, final MessageBus bus) {
    myProject = project;
    myBus = bus;
    myTaskQueue = new BackgroundTaskQueue(project, "Refreshing VCS history");
  }

  public int getInitialCount() {
    return myInitialCount;
  }

  public void setInitialCount(final int initialCount) {
    myInitialCount = initialCount;
  }

  public List<CommittedChangeList> getProjectChanges(final ChangeBrowserSettings settings, final int maxCount) throws VcsException {
    final VirtualFile[] files = ProjectLevelVcsManager.getInstance(myProject).getAllVersionedRoots();
    final LinkedHashSet<CommittedChangeList> result = new LinkedHashSet<CommittedChangeList>();
    for(VirtualFile file: files) {
      result.addAll(getChanges(settings, file, maxCount));
    }
    return new ArrayList<CommittedChangeList>(result);
  }

  public List<CommittedChangeList> getChanges(ChangeBrowserSettings settings, final VirtualFile file, final int maxCount)
    throws VcsException {
    final AbstractVcs vcs = ProjectLevelVcsManager.getInstance(myProject).getVcsFor(file);
    assert vcs != null;
    final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
    if (provider == null) {
      return Collections.emptyList();
    }
    final RepositoryLocation location = provider.getLocationFor(file);
    if (location == null) {
      return Collections.emptyList();
    }
    if (settings instanceof CompositeCommittedChangesProvider.CompositeChangeBrowserSettings) {
      settings = ((CompositeCommittedChangesProvider.CompositeChangeBrowserSettings) settings).get(vcs);
    }
    if (provider instanceof CachingCommittedChangesProvider) {
      final CachingCommittedChangesProvider cachingProvider = (CachingCommittedChangesProvider)provider;
      try {
        if (canGetFromCache(vcs, settings, file, location, maxCount)) {
          return getChangesWithCaching(vcs, settings, file, location, maxCount);
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    //noinspection unchecked
    return provider.getCommittedChanges(settings, location, maxCount);
  }

  private boolean canGetFromCache(final AbstractVcs vcs, final ChangeBrowserSettings settings,
                                  final VirtualFile root, final RepositoryLocation location, final int maxCount) throws IOException {
    ChangesCacheFile cacheFile = getCacheFile(vcs, root, location);
    if (cacheFile.isEmpty()) {
      return true;   // we'll initialize the cache and check again after that
    }
    if (settings.USE_DATE_BEFORE_FILTER && !settings.USE_DATE_AFTER_FILTER) {
      return cacheFile.hasCompleteHistory();
    }
    if (settings.USE_CHANGE_BEFORE_FILTER && !settings.USE_CHANGE_AFTER_FILTER) {
      return cacheFile.hasCompleteHistory();
    }

    boolean hasDateFilter = settings.USE_DATE_AFTER_FILTER || settings.USE_DATE_BEFORE_FILTER || settings.USE_CHANGE_AFTER_FILTER || settings.USE_CHANGE_BEFORE_FILTER;
    boolean hasNonDateFilter = settings.isNonDateFilterSpecified();
    if (!hasDateFilter && hasNonDateFilter) {
      return cacheFile.hasCompleteHistory();
    }
    if (settings.USE_DATE_AFTER_FILTER && settings.getDateAfter().getTime() < cacheFile.getFirstCachedDate().getTime()) {
      return cacheFile.hasCompleteHistory();
    }
    if (settings.USE_CHANGE_AFTER_FILTER && settings.getChangeAfterFilter().longValue() < cacheFile.getFirstCachedChangelist()) {
      return cacheFile.hasCompleteHistory();
    }
    return true;
  }

  public boolean hasCachesForAllRoots() {
    Collection<ChangesCacheFile> caches = getAllCaches();
    for(ChangesCacheFile cacheFile: caches) {
      try {
        if (cacheFile.isEmpty()) {
          return false;
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return true;
  }

  private Collection<ChangesCacheFile> getAllCaches() {
    Collection<ChangesCacheFile> result = new ArrayList<ChangesCacheFile>();
    final VirtualFile[] files = ProjectLevelVcsManager.getInstance(myProject).getAllVersionedRoots();
    for(VirtualFile file: files) {
      final AbstractVcs vcs = ProjectLevelVcsManager.getInstance(myProject).getVcsFor(file);
      assert vcs != null;
      final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
      if (provider instanceof CachingCommittedChangesProvider) {
        final RepositoryLocation location = provider.getLocationFor(file);
        if (location != null) {
          ChangesCacheFile cacheFile = getCacheFile(vcs, file, location);
          result.add(cacheFile);
        }
      }
    }
    return result;
  }

  private List<CommittedChangeList> getChangesWithCaching(final AbstractVcs vcs,
                                                          final ChangeBrowserSettings settings,
                                                          final VirtualFile root,
                                                          final RepositoryLocation location,
                                                          final int maxCount) throws VcsException, IOException {
    ChangesCacheFile cacheFile = getCacheFile(vcs, root, location);
    if (cacheFile.isEmpty()) {
      List<CommittedChangeList> changes = initCache(cacheFile);
      if (canGetFromCache(vcs, settings, root, location, maxCount)) {
        settings.filterChanges(changes);
        return trimToSize(changes, maxCount);
      }
      return cacheFile.getProvider().getCommittedChanges(settings, location, maxCount);
    }
    else {
      List<CommittedChangeList> changes = cacheFile.readChanges(settings, maxCount);
      List<CommittedChangeList> newChanges = refreshCache(cacheFile);
      settings.filterChanges(newChanges);
      changes.addAll(newChanges);
      return trimToSize(changes, maxCount);
    }
  }

  public void refreshAllCaches() throws IOException, VcsException {
    final Collection<ChangesCacheFile> files = getAllCaches();
    for(ChangesCacheFile file: files) {
      if (file.isEmpty()) {
        initCache(file);
      }
      else {
        refreshCache(file);
      }
    }
  }

  private List<CommittedChangeList> initCache(final ChangesCacheFile cacheFile) throws VcsException, IOException {
    LOG.info("Initializing cache for " + cacheFile.getLocation());
    final CachingCommittedChangesProvider provider = cacheFile.getProvider();
    final RepositoryLocation location = cacheFile.getLocation();
    List<CommittedChangeList> changes = provider.getCommittedChanges(provider.createDefaultSettings(), location, myInitialCount);
    // when initially initializing cache, assume all changelists are locally available
    cacheFile.writeChanges(changes, true); // this sorts changes in chronological order
    if (changes.size() < myInitialCount) {
      cacheFile.setHaveCompleteHistory(true);
    }
    return changes;
  }

  private List<CommittedChangeList> refreshCache(final ChangesCacheFile cacheFile) throws VcsException, IOException {
    LOG.info("Refreshing cache for " + cacheFile.getLocation());
    final Date date = cacheFile.getLastCachedDate();
    final CachingCommittedChangesProvider provider = cacheFile.getProvider();
    final RepositoryLocation location = cacheFile.getLocation();
    final ChangeBrowserSettings defaultSettings = provider.createDefaultSettings();
    defaultSettings.setDateAfter(date);
    defaultSettings.USE_DATE_AFTER_FILTER = true;
    List<CommittedChangeList> newChanges = provider.getCommittedChanges(defaultSettings, location, 0);
    newChanges = cacheFile.writeChanges(newChanges, false);    // skip duplicates
    if (newChanges.size() > 0) {
      myBus.syncPublisher(COMMITTED_TOPIC).changesLoaded(location, newChanges);
    }
    return newChanges;
  }

  private static List<CommittedChangeList> trimToSize(final List<CommittedChangeList> changes, final int maxCount) {
    if (maxCount > 0) {
      while(changes.size() > maxCount) {
        changes.remove(0);
      }
    }
    return changes;
  }

  public List<CommittedChangeList> getIncomingChanges() {
    final List<CommittedChangeList> result = new ArrayList<CommittedChangeList>();
    final Collection<ChangesCacheFile> caches = getAllCaches();
    for(ChangesCacheFile cache: caches) {
      try {
        if (!cache.isEmpty()) {
          LOG.info("Loading incoming changes for " + cache.getLocation());
          result.addAll(cache.loadIncomingChanges());
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return result;
  }

  public void processUpdatedFiles(final UpdatedFiles updatedFiles) {
    final Collection<ChangesCacheFile> caches = getAllCaches();
    for(final ChangesCacheFile cache: caches) {
      boolean needRefresh;
      try {
        needRefresh = cache.processUpdatedFiles(updatedFiles);
      }
      catch (IOException e) {
        LOG.error(e);
        continue;
      }
      if (needRefresh) {
        refreshCacheAsync(cache, new Runnable() {
          public void run() {
            try {
              cache.processUpdatedFiles(updatedFiles);
            }
            catch (IOException e) {
              LOG.error(e);
            }
          }
        });
      }
    }
    notifyIncomingChangesUpdated();
  }

  private void notifyIncomingChangesUpdated() {
    myBus.syncPublisher(COMMITTED_TOPIC).incomingChangesUpdated();
  }

  public boolean isRefreshingIncomingChanges() {
    return myRefreshingIncomingChanges;
  }

  public boolean refreshIncomingChanges() {
    boolean hasChanges = false;
    final Collection<ChangesCacheFile> caches = getAllCaches();
    for(ChangesCacheFile file: caches) {
      LOG.info("Refreshing incoming changes for " + file.getLocation());
      try {
        boolean changesForCache = file.refreshIncomingChanges();
        hasChanges |= changesForCache;
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return hasChanges;
  }

  public void refreshIncomingChangesAsync() {
    LOG.info("Refreshing incoming changes in background");
    myRefreshingIncomingChanges = true;
    final Task.Backgroundable task = new Task.Backgroundable(myProject, "Refreshing incoming changes") {
      private boolean myAnyChanges = false;

      public void run(final ProgressIndicator indicator) {
        myAnyChanges = refreshIncomingChanges();
      }

      public void onSuccess() {
        myRefreshingIncomingChanges = false;
        if (myAnyChanges) {
          notifyIncomingChangesUpdated();
        }
      }
    };
    myTaskQueue.run(task);
  }

  private void refreshCacheAsync(final ChangesCacheFile cache, final Runnable postRunnable) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      refreshCacheSync(cache, postRunnable);
      return;
    }
    final Task.Backgroundable task = new Task.Backgroundable(myProject, "Refreshing VCS history") {
      private boolean hasNewChanges = false;

      public void run(final ProgressIndicator indicator) {
        try {
          final List<CommittedChangeList> list = refreshCache(cache);
          hasNewChanges = (list.size() > 0);
        }
        catch (Exception e) {
          LOG.error(e);
        }
      }

      public void onSuccess() {
        if (postRunnable != null && hasNewChanges) {
          postRunnable.run();
        }
      }
    };
    myTaskQueue.run(task);
  }

  private void refreshCacheSync(final ChangesCacheFile cache, final Runnable postRunnable) {
    final List<CommittedChangeList> list;
    try {
      list = refreshCache(cache);
      if (list.size() > 0 && postRunnable != null) {
        postRunnable.run();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ChangesCacheFile getCacheFile(AbstractVcs vcs, VirtualFile root, RepositoryLocation location) {
    ChangesCacheFile cacheFile = myCacheFiles.get(location);
    if (cacheFile == null) {
      cacheFile = new ChangesCacheFile(myProject, getCachePath(location), vcs, root, location);
      myCacheFiles.put(location, cacheFile);
    }
    return cacheFile;
  }

  public File getCacheBasePath() {
    File file = new File(PathManager.getSystemPath(), VCS_CACHE_PATH);
    file = new File(file, myProject.getLocationHash());
    return file;
  }

  private File getCachePath(final RepositoryLocation location) {
    File file = getCacheBasePath();
    file.mkdirs();
    String s = location.toString();
    try {
      final byte[] bytes = MessageDigest.getInstance("MD5").digest(CharsetToolkit.getUtf8Bytes(s));
      StringBuilder result = new StringBuilder();
      for (byte aByte : bytes) {
        result.append(String.format("%02x", aByte));
      }
      return new File(file, result.toString());
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}