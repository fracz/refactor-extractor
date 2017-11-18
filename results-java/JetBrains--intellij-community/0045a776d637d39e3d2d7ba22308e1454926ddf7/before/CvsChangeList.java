/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 28.11.2006
 * Time: 20:44:46
 */
package com.intellij.cvsSupport2.changeBrowser;

import com.intellij.cvsSupport2.connections.CvsEnvironment;
import com.intellij.cvsSupport2.history.CvsRevisionNumber;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.annotations.NonNls;
import org.netbeans.lib.cvsclient.command.log.Revision;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CvsChangeList implements CommittedChangeList {
  private long myDate;
  private long myFinishDate;

  private final long myNumber;
  private final String myDescription;

  private final String myRootPath;

  private final List<RevisionWrapper> myRevisions = new ArrayList<RevisionWrapper>();

  private final String myUser;
  private static final int SUITABLE_DIFF = 2 * 60 * 1000;
  private final CvsEnvironment myEnvironment;
  private final Project myProject;
  private List<Change> myChanges;
  @NonNls static final String EXP_STATE = "Exp";
  @NonNls public static final String ADDED_STATE = "added";
  @NonNls static final String DEAD_STATE = "dead";


  public CvsChangeList(final long number, final String description, final long date,
                       String user, String rootPath, final CvsEnvironment environment, final Project project) {
    myDate = date;
    myFinishDate = date;
    myNumber = number;
    myDescription = description;
    myUser = user;
    myRootPath = rootPath;
    myEnvironment = environment;
    myProject = project;
  }

  public String getCommitterName() {
    return myUser;
  }

  public Date getCommitDate() {
    return new Date(myDate);
  }

  public long getNumber() {
    return myNumber;
  }

  public Collection<Change> getChanges() {
    if (myChanges == null) {
      myChanges = new ArrayList<Change>();
      for(RevisionWrapper wrapper: myRevisions) {
        final Revision revision = wrapper.getRevision();
        final String state = revision.getState();
        ContentRevision beforeRevision = isAdded(revision)
          ? null
          : new CvsContentRevision(new File(wrapper.getFile()), new CvsRevisionNumber(revision.getNumber()).getPrevNumber().asString(),
                                   myEnvironment, myProject);
        ContentRevision afterRevision = (DEAD_STATE.equals(state))
          ? null
          : new CvsContentRevision(new File(wrapper.getFile()), revision.getNumber(), myEnvironment, myProject);
        myChanges.add(new Change(beforeRevision, afterRevision));
      }
    }
    return myChanges;
  }

  public String getName() {
    return myDescription;
  }

  public String getComment() {
    return myDescription;
  }

  public boolean containsDate(long date) {
    if (date >= myDate && date <= myFinishDate) {
      return true;
    }

    if (Math.abs(date - myDate) < SUITABLE_DIFF) {
      return true;
    }

    if (Math.abs(date - myFinishDate) < SUITABLE_DIFF) {
      return true;
    }

    return false;
  }

  public void addFileRevision(RevisionWrapper revision) {
    myRevisions.add(revision);

    final long revisionTime = revision.getTime();

    if (revisionTime < myDate) {
      myDate = revisionTime;
    }

    if (revisionTime > myFinishDate) {
      myFinishDate = revisionTime;
    }

  }

  public static boolean isAdded(final Revision revision) {
    final String revisionState = revision.getState();
    if (EXP_STATE.equals(revisionState) && revision.getLines() == null) {
      return true;
    }
    if (ADDED_STATE.equals(revisionState)) {
      return true;
    }
    return false;
  }

  public static boolean isAncestor(final String parent, final String child) {
    return child.startsWith(parent + "/");
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CvsChangeList that = (CvsChangeList)o;

    if (!myRevisions.equals(that.myRevisions)) return false;

    return true;
  }

  public int hashCode() {
    return myRevisions.hashCode();
  }
}