/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.cvsSupport2.changeBrowser;

import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.project.Project;
import com.intellij.cvsSupport2.connections.CvsEnvironment;
import com.intellij.cvsSupport2.cvsoperations.dateOrRevision.RevisionOrDate;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author yole
 */
public class CvsBinaryContentRevision extends CvsContentRevision implements BinaryContentRevision {
  private byte[] myContent;

  public CvsBinaryContentRevision(final File file,
                                  final RevisionOrDate revision,
                                  final CvsEnvironment environment,
                                  final Project project) {
    super(file, revision, environment, project);
  }

  @Nullable
  public byte[] getBinaryContent() throws VcsException {
    if (myContent == null) {
      myContent = loadContent();
    }
    return myContent;
  }
}