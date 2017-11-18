/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.projectimport.wizard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;

import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Json RPC based implementation of the {@link ProjectNotificationSubscriber} which notifies user
 * about output events via popup notification.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@Singleton
public class ProjectImportOutputJsonRpcNotifier implements ProjectNotificationSubscriber {

    private final NotificationManager                  notificationManager;
    private final ProjectImportOutputJsonRpcSubscriber subscriber;
    private final CoreLocalizationConstant             locale;

    private StatusNotification singletonNotification;
    private String             projectName;

    @Inject
    public ProjectImportOutputJsonRpcNotifier(NotificationManager notificationManager,
                                              ProjectImportOutputJsonRpcSubscriber subscriber,
                                              CoreLocalizationConstant locale,
                                              EventBus eventBus) {
        this.notificationManager = notificationManager;
        this.subscriber = subscriber;
        this.locale = locale;

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                subscriber.unSubscribeForImportOutputEvents();

                singletonNotification.setStatus(FAIL);
                singletonNotification.setContent("");
            }
        });
    }

    @Override
    public void subscribe(String projectName, StatusNotification notification) {
        this.projectName = projectName;
        this.singletonNotification = notification;

        subscriber.subscribeForImportOutputEvents(progressRecord -> {
            ProjectImportOutputJsonRpcNotifier.this.projectName = nullToEmpty(progressRecord.getProjectName());
            singletonNotification.setTitle(locale.importingProject(ProjectImportOutputJsonRpcNotifier.this.projectName));
            singletonNotification.setContent(nullToEmpty(progressRecord.getLine()));
        });
    }

    @Override
    public void subscribe(String projectName) {
        singletonNotification = notificationManager.notify(locale.importingProject(projectName), PROGRESS, FLOAT_MODE);
        subscribe(projectName, singletonNotification);
    }

    @Override
    public void onSuccess() {
        subscriber.unSubscribeForImportOutputEvents();

        singletonNotification.setStatus(SUCCESS);
        singletonNotification.setTitle(locale.importProjectMessageSuccess(projectName));
        singletonNotification.setContent("");
    }

    @Override
    public void onFailure(String errorMessage) {
        subscriber.unSubscribeForImportOutputEvents();

        singletonNotification.setStatus(FAIL);
        singletonNotification.setContent(errorMessage);
    }
}