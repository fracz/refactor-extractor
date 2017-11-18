/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
 * Date: 28.11.2006
 * Time: 17:20:32
 */
package org.jetbrains.idea.svn.history;

import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.diagnostic.Logger;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnVcs;

import java.util.*;

public class SvnChangeList implements CommittedChangeList {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.history");

  private SvnVcs myVcs;
  private final String myRepositoryURL;
  private long myRevision;
  private String myAuthor;
  private Date myDate;
  private String myMessage;
  private Set<String> myChangedPaths = new HashSet<String>();
  private Set<String> myAddedPaths = new HashSet<String>();
  private Set<String> myDeletedPaths = new HashSet<String>();
  private List<Change> myChanges;

  public SvnChangeList(SvnVcs vcs, final SVNLogEntry logEntry, SVNRepository repository) {
    myVcs = vcs;
    myRevision = logEntry.getRevision();
    myAuthor = logEntry.getAuthor();
    myDate = logEntry.getDate();
    myMessage = logEntry.getMessage();
    for(Object o: logEntry.getChangedPaths().values()) {
      SVNLogEntryPath entry = (SVNLogEntryPath) o;
      if (entry.getType() == 'A') {
        myAddedPaths.add(entry.getPath());
      }
      else if (entry.getType() == 'D') {
        myDeletedPaths.add(entry.getPath());
      }
      else {
        myChangedPaths.add(entry.getPath());
      }
    }
    myRepositoryURL = repository.getLocation().toString();
  }

  public String getCommitterName() {
    return myAuthor;
  }

  public Date getCommitDate() {
    return myDate;
  }

  public Collection<Change> getChanges() {
    if (myChanges == null) {
      loadChanges();
    }
    return myChanges;
  }

  private void loadChanges() {
    myChanges = new ArrayList<Change>();
    SVNRepository repository;
    try {
      repository = myVcs.createRepository(myRepositoryURL);
    }
    catch (SVNException e) {
      // should never happen - we got the URL from a real live existing repository
      LOG.error(e);
      return;
    }
    for(String path: myAddedPaths) {
      myChanges.add(new Change(null,
                               new SvnRepositoryContentRevision(repository, path, myRevision)));
    }
    for(String path: myDeletedPaths) {
      myChanges.add(new Change(new SvnRepositoryContentRevision(repository, path, myRevision-1),
                               null));

    }
    for(String path: myChangedPaths) {
      SvnRepositoryContentRevision beforeRevision = new SvnRepositoryContentRevision(repository, path, myRevision-1);
      SvnRepositoryContentRevision afterRevision = new SvnRepositoryContentRevision(repository, path, myRevision);
      myChanges.add(new Change(beforeRevision, afterRevision));
    }
  }

  @NotNull
  public String getName() {
    return myMessage;
  }

  public String getComment() {
    return myMessage;
  }

  public long getNumber() {
    return myRevision;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SvnChangeList that = (SvnChangeList)o;

    if (myRevision != that.myRevision) return false;
    if (myAuthor != null ? !myAuthor.equals(that.myAuthor) : that.myAuthor != null) return false;
    if (myDate != null ? !myDate.equals(that.myDate) : that.myDate != null) return false;
    if (myMessage != null ? !myMessage.equals(that.myMessage) : that.myMessage != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (int)(myRevision ^ (myRevision >>> 32));
    result = 31 * result + (myAuthor != null ? myAuthor.hashCode() : 0);
    result = 31 * result + (myDate != null ? myDate.hashCode() : 0);
    result = 31 * result + (myMessage != null ? myMessage.hashCode() : 0);
    return result;
  }
}