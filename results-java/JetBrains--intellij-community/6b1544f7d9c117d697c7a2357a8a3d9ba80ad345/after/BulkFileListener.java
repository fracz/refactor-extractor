/*
 * @author max
 */
package com.intellij.openapi.vfs.newvfs;

import com.intellij.openapi.vfs.newvfs.events.VFileEvent;

import java.util.List;

public interface BulkFileListener {
  void before(List<VFileEvent> events);
  void after(List<VFileEvent> events);
}