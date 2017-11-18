/*
 * Druid - a distributed column store.
 * Copyright (C) 2012, 2013  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.druid.indexing.overlord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.metamx.common.Pair;
import com.metamx.common.lifecycle.LifecycleStart;
import com.metamx.common.lifecycle.LifecycleStop;
import com.metamx.emitter.EmittingLogger;
import io.druid.indexing.common.TaskStatus;
import io.druid.metadata.MetadataStorageConnector;
import io.druid.indexing.common.TaskLock;
import io.druid.indexing.common.actions.TaskAction;
import io.druid.indexing.common.config.TaskStorageConfig;
import io.druid.indexing.common.task.Task;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MetadataTaskStorage implements TaskStorage
{
  private static final MetadataStorageActionHandlerTypes<Task, TaskStatus, TaskAction, TaskLock> TASK_TYPES = new MetadataStorageActionHandlerTypes<Task, TaskStatus, TaskAction, TaskLock>()
  {
    @Override
    public TypeReference<Task> getTaskType()
    {
      return new TypeReference<Task>()
      {
      };
    }

    @Override
    public TypeReference<TaskStatus> getTaskStatusType()
    {
      return new TypeReference<TaskStatus>()
      {
      };
    }

    @Override
    public TypeReference<TaskAction> getTaskActionType()
    {
      return new TypeReference<TaskAction>()
      {
      };
    }

    @Override
    public TypeReference<TaskLock> getTaskLockType()
    {
      return new TypeReference<TaskLock>()
      {
      };
    }
  };
  private final MetadataStorageConnector metadataStorageConnector;
  private final TaskStorageConfig config;
  private final MetadataStorageActionHandler<Task, TaskStatus, TaskAction, TaskLock> handler;

  private static final EmittingLogger log = new EmittingLogger(MetadataTaskStorage.class);

  @Inject
  public MetadataTaskStorage(
      final MetadataStorageConnector metadataStorageConnector,
      final TaskStorageConfig config,
      final MetadataStorageActionHandlerFactory factory
  )
  {
    this.metadataStorageConnector = metadataStorageConnector;
    this.config = config;
    this.handler = factory.create(TASK_TYPES);
  }

  @LifecycleStart
  public void start()
  {
    metadataStorageConnector.createTaskTables();
  }

  @LifecycleStop
  public void stop()
  {
    // do nothing
  }

  @Override
  public void insert(final Task task, final TaskStatus status) throws TaskExistsException
  {
    Preconditions.checkNotNull(task, "task");
    Preconditions.checkNotNull(status, "status");
    Preconditions.checkArgument(
        task.getId().equals(status.getId()),
        "Task/Status ID mismatch[%s/%s]",
        task.getId(),
        status.getId()
    );

    log.info("Inserting task %s with status: %s", task.getId(), status);

    try {
      handler.insert(
          task.getId(),
          new DateTime(),
          task.getDataSource(),
          task,
          status.isRunnable(),
          status
      );
    }
    catch (Exception e) {
      if(e instanceof TaskExistsException) {
        throw (TaskExistsException) e;
      } else {
        Throwables.propagate(e);
      }
    }
  }

  @Override
  public void setStatus(final TaskStatus status)
  {
    Preconditions.checkNotNull(status, "status");

    log.info("Updating task %s to status: %s", status.getId(), status);

    final boolean set = handler.setStatus(
        status.getId(),
        status.isRunnable(),
        status
    );
    if (!set) {
      throw new IllegalStateException(String.format("Active task not found: %s", status.getId()));
    }
  }

  @Override
  public Optional<Task> getTask(final String taskId)
  {
      return handler.getTask(taskId);
  }

  @Override
  public Optional<TaskStatus> getStatus(final String taskId)
  {
    return handler.getTaskStatus(taskId);
  }

  @Override
  public List<Task> getActiveTasks()
  {
    return ImmutableList.copyOf(
        Iterables.transform(
            Iterables.filter(
                handler.getActiveTasksWithStatus(),
                new Predicate<Pair<Task, TaskStatus>>()
                {
                  @Override
                  public boolean apply(
                      @Nullable Pair<Task, TaskStatus> input
                  )
                  {
                    return input.rhs.isRunnable();
                  }
                }
            ),
            new Function<Pair<Task, TaskStatus>, Task>()
            {
              @Nullable
              @Override
              public Task apply(@Nullable Pair<Task, TaskStatus> input)
              {
                return input.lhs;
              }
            }
        )
    );
  }

  @Override
  public List<TaskStatus> getRecentlyFinishedTaskStatuses()
  {
    final DateTime start = new DateTime().minus(config.getRecentlyFinishedThreshold());

    return ImmutableList.copyOf(
        Iterables.filter(
            handler.getRecentlyFinishedTaskStatuses(start),
            new Predicate<TaskStatus>()
            {
              @Override
              public boolean apply(TaskStatus status)
              {
                return status.isComplete();
              }
            }
        )
    );
  }

  @Override
  public void addLock(final String taskid, final TaskLock taskLock)
  {
    Preconditions.checkNotNull(taskid, "taskid");
    Preconditions.checkNotNull(taskLock, "taskLock");

    log.info(
        "Adding lock on interval[%s] version[%s] for task: %s",
        taskLock.getInterval(),
        taskLock.getVersion(),
        taskid
    );

    handler.addLock(taskid, taskLock);
  }

  @Override
  public void removeLock(String taskid, TaskLock taskLockToRemove)
  {
    Preconditions.checkNotNull(taskid, "taskid");
    Preconditions.checkNotNull(taskLockToRemove, "taskLockToRemove");

    final Map<Long, TaskLock> taskLocks = getLocksWithIds(taskid);

    for (final Map.Entry<Long, TaskLock> taskLockWithId : taskLocks.entrySet()) {
      final long id = taskLockWithId.getKey();
      final TaskLock taskLock = taskLockWithId.getValue();

      if (taskLock.equals(taskLockToRemove)) {
        log.info("Deleting TaskLock with id[%d]: %s", id, taskLock);
        handler.removeLock(id);
      }
    }
  }

  @Override
  public List<TaskLock> getLocks(String taskid)
  {
    return ImmutableList.copyOf(
        Iterables.transform(
            getLocksWithIds(taskid).entrySet(), new Function<Map.Entry<Long, TaskLock>, TaskLock>()
            {
              @Override
              public TaskLock apply(Map.Entry<Long, TaskLock> e)
              {
                return e.getValue();
              }
            }
        )
    );
  }

  @Override
  public <T> void addAuditLog(final Task task, final TaskAction<T> taskAction)
  {
    Preconditions.checkNotNull(taskAction, "taskAction");

    log.info("Logging action for task[%s]: %s", task.getId(), taskAction);

    handler.addAuditLog(task.getId(), taskAction);
  }

  @Override
  public List<TaskAction> getAuditLogs(final String taskId)
  {
    return handler.getTaskLogs(taskId);
  }

  private Map<Long, TaskLock> getLocksWithIds(final String taskid)
  {
    return handler.getTaskLocks(taskid);
  }
}