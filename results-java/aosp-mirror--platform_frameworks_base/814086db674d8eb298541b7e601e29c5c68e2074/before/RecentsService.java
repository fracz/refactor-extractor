/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recents;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskViewTransform;

import java.lang.ref.WeakReference;


/** The message handler to process Recents SysUI messages */
class SystemUIMessageHandler extends Handler {
    WeakReference<Context> mContext;

    SystemUIMessageHandler(Context context) {
        // Keep a weak ref to the context instead of a strong ref
        mContext = new WeakReference<Context>(context);
    }

    @Override
    public void handleMessage(Message msg) {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake,
                "[RecentsService|handleMessage]", msg);

        Context context = mContext.get();
        if (context == null) return;

        if (msg.what == AlternateRecentsComponent.MSG_UPDATE_FOR_CONFIGURATION) {
            RecentsTaskLoader.initialize(context);
            RecentsConfiguration.reinitialize(context);

            try {
                Bundle data = msg.getData();
                Rect windowRect = data.getParcelable(AlternateRecentsComponent.KEY_WINDOW_RECT);
                Rect systemInsets = data.getParcelable(AlternateRecentsComponent.KEY_SYSTEM_INSETS);

                // NOTE: None of the rects computed below need to be offset for the status bar,
                // since that is done when we compute the animation itself in the Recents component

                // Create a dummy task stack & compute the rect for the thumbnail to animate to
                TaskStack stack = new TaskStack(context);
                TaskStackView tsv = new TaskStackView(context, stack);
                Bundle replyData = new Bundle();
                TaskViewTransform transform;

                // Get the task stack and search bar bounds
                Rect taskStackBounds = new Rect();
                RecentsConfiguration config = RecentsConfiguration.getInstance();
                config.getTaskStackBounds(windowRect.width(), windowRect.height(), taskStackBounds);

                // Calculate the target task rect for when there is one task.

                // NOTE: Since the nav bar height is already accounted for in the windowRect, don't
                // pass in a left or bottom inset
                stack.addTask(new Task());
                tsv.computeRects(taskStackBounds.width(), taskStackBounds.height() -
                        systemInsets.top - systemInsets.bottom, 0, 0);
                tsv.boundScroll();
                transform = tsv.getStackTransform(0, tsv.getStackScroll());
                transform.rect.offset(taskStackBounds.left, taskStackBounds.top);
                replyData.putParcelable(AlternateRecentsComponent.KEY_SINGLE_TASK_STACK_RECT,
                        new Rect(transform.rect));

                // Also calculate the target task rect when there are multiple tasks.
                stack.addTask(new Task());
                tsv.computeRects(taskStackBounds.width(), taskStackBounds.height() -
                        systemInsets.top - systemInsets.bottom, 0, 0);
                tsv.setStackScrollRaw(Integer.MAX_VALUE);
                tsv.boundScroll();
                transform = tsv.getStackTransform(1, tsv.getStackScroll());
                transform.rect.offset(taskStackBounds.left, taskStackBounds.top);
                replyData.putParcelable(AlternateRecentsComponent.KEY_MULTIPLE_TASK_STACK_RECT,
                        new Rect(transform.rect));

                data.putParcelable(AlternateRecentsComponent.KEY_CONFIGURATION_DATA, replyData);
                Message reply = Message.obtain(null,
                        AlternateRecentsComponent.MSG_UPDATE_FOR_CONFIGURATION, 0, 0);
                reply.setData(data);
                msg.replyTo.send(reply);
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        } else if (msg.what == AlternateRecentsComponent.MSG_CLOSE_RECENTS) {
            // Do nothing
        } else if (msg.what == AlternateRecentsComponent.MSG_TOGGLE_RECENTS) {
            // Send a broadcast to toggle recents
            Intent intent = new Intent(RecentsService.ACTION_TOGGLE_RECENTS_ACTIVITY);
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);

            // Time this path
            Console.logTraceTime(Constants.DebugFlags.App.TimeRecentsStartup,
                    Constants.DebugFlags.App.TimeRecentsStartupKey, "receivedToggleRecents");
            Console.logTraceTime(Constants.DebugFlags.App.TimeRecentsLaunchTask,
                    Constants.DebugFlags.App.TimeRecentsLaunchKey, "receivedToggleRecents");
        }
    }
}

/* Service */
public class RecentsService extends Service {
    final static String ACTION_TOGGLE_RECENTS_ACTIVITY = "action_toggle_recents_activity";

    Messenger mSystemUIMessenger = new Messenger(new SystemUIMessageHandler(this));

    @Override
    public void onCreate() {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake, "[RecentsService|onCreate]");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake, "[RecentsService|onBind]");
        return mSystemUIMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake, "[RecentsService|onUnbind]");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake, "[RecentsService|onRebind]");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Console.log(Constants.DebugFlags.App.SystemUIHandshake, "[RecentsService|onDestroy]");
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }
}