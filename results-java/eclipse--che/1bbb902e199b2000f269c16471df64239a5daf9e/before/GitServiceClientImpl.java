/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.git;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchDeleteRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.ConfigRequest;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitUrlVendorInfo;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.RmRequest;
import org.eclipse.che.api.git.shared.ShowFileContentRequest;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentMessageBusProvider;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.api.git.shared.StatusFormat.PORCELAIN;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;
import static org.eclipse.che.ide.util.Arrays.isNullOrEmpty;

/**
 * Implementation of the {@link GitServiceClient}.
 *
 * @author Ann Zhuleva
 * @author Valeriy Svydenko
 */
@Singleton
public class GitServiceClientImpl implements GitServiceClient {
    public static final String ADD               = "/git/add";
    public static final String BRANCH_LIST       = "/git/branch-list";
    public static final String CHECKOUT          = "/git/checkout";
    public static final String BRANCH_CREATE     = "/git/branch-create";
    public static final String BRANCH_DELETE     = "/git/branch-delete";
    public static final String BRANCH_RENAME     = "/git/branch-rename";
    public static final String CLONE             = "/git/clone";
    public static final String COMMIT            = "/git/commit";
    public static final String CONFIG            = "/git/config";
    public static final String DIFF              = "/git/diff";
    public static final String FETCH             = "/git/fetch";
    public static final String INIT              = "/git/init";
    public static final String LOG               = "/git/log";
    public static final String SHOW              = "/git/show";
    public static final String MERGE             = "/git/merge";
    public static final String STATUS            = "/git/status";
    public static final String PUSH              = "/git/push";
    public static final String PULL              = "/git/pull";
    public static final String REMOTE_LIST       = "/git/remote-list";
    public static final String REMOTE_ADD        = "/git/remote-add";
    public static final String REMOTE_DELETE     = "/git/remote-delete";
    public static final String REMOVE            = "/git/rm";
    public static final String RESET             = "/git/reset";
    public static final String COMMITERS         = "/git/commiters";
    public static final String DELETE_REPOSITORY = "/git/delete-repository";

    /** Loader to be displayed. */
    private final AsyncRequestLoader        loader;
    private final DtoFactory                dtoFactory;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private final AsyncRequestFactory       asyncRequestFactory;
    private final WsAgentMessageBusProvider wsAgentMessageBusProvider;
    private final AppContext                appContext;

    @Inject
    protected GitServiceClientImpl(LoaderFactory loaderFactory,
                                   WsAgentMessageBusProvider wsAgentMessageBusProvider,
                                   DtoFactory dtoFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AppContext appContext) {
        this.wsAgentMessageBusProvider = wsAgentMessageBusProvider;
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void init(DevMachine devMachine, ProjectConfigDto project, boolean bare, final RequestCallback<Void> callback)
            throws WebSocketException {
        InitRequest initRequest = dtoFactory.createDto(InitRequest.class);
        initRequest.setBare(bare);
        initRequest.setWorkingDir(project.getName());

        String url = INIT + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(initRequest)).header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> init(DevMachine devMachine, final Path project, final boolean bare) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                InitRequest initRequest = dtoFactory.createDto(InitRequest.class);
                initRequest.setBare(bare);
                initRequest.setWorkingDir(project.toString());

                String url = INIT + "?projectPath=" + project.toString();

                MessageBuilder builder = new MessageBuilder(POST, url);
                builder.data(dtoFactory.toJson(initRequest)).header(CONTENTTYPE, APPLICATION_JSON);
                Message message = builder.build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void cloneRepository(DevMachine devMachine,
                                ProjectConfigDto project,
                                String remoteUri,
                                String remoteName,
                                RequestCallback<RepoInfo> callback) throws WebSocketException {
        CloneRequest cloneRequest = dtoFactory.createDto(CloneRequest.class)
                                              .withRemoteName(remoteName)
                                              .withRemoteUri(remoteUri)
                                              .withWorkingDir(project.getPath());

        String params = "?projectPath=" + project.getPath();

        String url = CLONE + params;

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(cloneRequest))
               .header(CONTENTTYPE, APPLICATION_JSON)
               .header(ACCEPT, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    private void sendMessageToWS(final @NotNull Message message, final @NotNull RequestCallback<?> callback) {
        wsAgentMessageBusProvider.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus arg) throws OperationException {
                try {
                    arg.send(message, callback);
                } catch (WebSocketException e) {
                    throw new OperationException(e.getMessage(), e);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void statusText(DevMachine devMachine, ProjectConfigDto project, StatusFormat format, AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS;
        String params = "?projectPath=" + project.getPath() + "&format=" + format;

        asyncRequestFactory.createPostRequest(url + params, null)
                           .loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public Promise<String> statusText(DevMachine devMachine, Path project, StatusFormat format) {
        String url = devMachine.getWsAgentBaseUrl() + STATUS;
        String params = "?projectPath=" + project + "&format=" + format;

        return asyncRequestFactory.createPostRequest(url + params, null)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, TEXT_PLAIN)
                                  .send(new StringUnmarshaller());
    }

    /** {@inheritDoc} */
    @Override
    public void add(DevMachine devMachine,
                    ProjectConfig project,
                    boolean update,
                    @Nullable List<String> filePattern,
                    RequestCallback<Void> callback) throws WebSocketException {
        AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);
        if (filePattern == null) {
            addRequest.setFilepattern(AddRequest.DEFAULT_PATTERN);
        } else {
            addRequest.setFilepattern(filePattern);
        }
        String url = ADD + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(addRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> add(final DevMachine devMachine, final Path project, final boolean update, final Path[] paths) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                final AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);

                if (paths == null) {
                    addRequest.setFilepattern(AddRequest.DEFAULT_PATTERN);
                } else {

                    final List<String> patterns = new ArrayList<>(); //need for compatible with server side
                    for (Path path : paths) {
                        patterns.add(path.isEmpty() ? "." : path.toString());
                    }

                    addRequest.setFilepattern(patterns);
                }

                final String url = ADD + "?projectPath=" + project.toString();
                final Message message = new MessageBuilder(POST, url).data(dtoFactory.toJson(addRequest))
                                                                     .header(CONTENTTYPE, APPLICATION_JSON)
                                                                     .build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void commit(DevMachine devMachine,
                       ProjectConfig project,
                       String message,
                       boolean all,
                       boolean amend,
                       AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Revision> commit(DevMachine devMachine, Path project, String message, boolean all, boolean amend) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = devMachine.getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    @Override
    public void commit(final DevMachine devMachine,
                       final ProjectConfigDto project,
                       final String message,
                       final List<String> files,
                       final boolean amend,
                       final AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(false)
                                                .withFiles(files);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Revision> commit(DevMachine devMachine, Path project, String message, Path[] files, boolean amend) {

        List<String> paths = new ArrayList<>(files.length);

        for (Path file : files) {
            paths.add(file.isEmpty() ? "." : file.toString());
        }

        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(false)
                                                .withFiles(paths);
        String url = devMachine.getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    /** {@inheritDoc} */
    @Override
    public void config(DevMachine devMachine,
                       ProjectConfigDto project,
                       @Nullable List<String> entries,
                       boolean all,
                       AsyncRequestCallback<Map<String, String>> callback) {
        ConfigRequest configRequest = dtoFactory.createDto(ConfigRequest.class)
                                                .withGetAll(all)
                                                .withConfigEntry(entries);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CONFIG + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, configRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Map<String, String>> config(DevMachine devMachine, Path project, List<String> entries, boolean all) {
        ConfigRequest configRequest = dtoFactory.createDto(ConfigRequest.class)
                                                .withGetAll(all)
                                                .withConfigEntry(entries);
        String url = devMachine.getWsAgentBaseUrl() + CONFIG + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, configRequest).loader(loader).send(new StringMapUnmarshaller());
    }

    /** {@inheritDoc} */
    @Override
    public void push(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> refSpec,
                     String remote,
                     boolean force,
                     AsyncRequestCallback<PushResponse> callback) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class).withRemote(remote).withRefSpec(refSpec).withForce(force);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PUSH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pushRequest).send(callback);
    }

    @Override
    public Promise<PushResponse> push(DevMachine devMachine, ProjectConfig project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevMachine().getWsAgentBaseUrl() + PUSH +
                                                     "?projectPath=" + project.getPath(), pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public Promise<PushResponse> push(DevMachine devMachine, Path project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(devMachine.getWsAgentBaseUrl() + PUSH + "?projectPath=" + project, pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    /** {@inheritDoc} */
    @Override
    public void remoteList(DevMachine devMachine,
                           ProjectConfigDto project,
                           @Nullable String remoteName,
                           boolean verbose,
                           AsyncRequestCallback<List<Remote>> callback) {
        RemoteListRequest remoteListRequest = dtoFactory.createDto(RemoteListRequest.class).withVerbose(verbose);
        if (remoteName != null) {
            remoteListRequest.setRemote(remoteName);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE_LIST + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, remoteListRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<Remote>> remoteList(DevMachine devMachine, ProjectConfig project, @Nullable String remoteName, boolean verbose) {
        RemoteListRequest remoteListRequest = dtoFactory.createDto(RemoteListRequest.class).withVerbose(verbose);
        if (remoteName != null) {
            remoteListRequest.setRemote(remoteName);
        }
        String url = devMachine.getWsAgentBaseUrl() + REMOTE_LIST + "?projectPath=" + project.getPath();
        return asyncRequestFactory.createPostRequest(url, remoteListRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    public Promise<List<Remote>> remoteList(DevMachine devMachine, Path project, String remote, boolean verbose) {
        RemoteListRequest remoteListRequest = dtoFactory.createDto(RemoteListRequest.class).withVerbose(verbose);
        if (remote != null) {
            remoteListRequest.setRemote(remote);
        }
        String url = devMachine.getWsAgentBaseUrl() + REMOTE_LIST + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, remoteListRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    /** {@inheritDoc} */
    @Override
    public void branchList(DevMachine devMachine,
                           ProjectConfig project,
                           @Nullable String remoteMode,
                           AsyncRequestCallback<List<Branch>> callback) {
        BranchListRequest branchListRequest = dtoFactory.createDto(BranchListRequest.class).withListMode(remoteMode);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH_LIST + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchListRequest).send(callback);
    }

    @Override
    public Promise<List<Branch>> branchList(DevMachine devMachine, Path project, String mode) {
        BranchListRequest branchListRequest = dtoFactory.createDto(BranchListRequest.class).withListMode(mode);
        String url = devMachine.getWsAgentBaseUrl() + BRANCH_LIST + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, branchListRequest).send(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class));
    }

    @Override
    public Promise<Status> status(DevMachine devMachine, ProjectConfig project) {
        final String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        final String url = devMachine.getWsAgentBaseUrl() + STATUS + params;
        return asyncRequestFactory.createPostRequest(url, null)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    @Override
    public Promise<Status> getStatus(DevMachine devMachine, Path project) {
        final String params = "?projectPath=" + project.toString() + "&format=" + PORCELAIN;
        final String url = devMachine.getWsAgentBaseUrl() + STATUS + params;
        return asyncRequestFactory.createPostRequest(url, null)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    /** {@inheritDoc} */
    @Override
    public void status(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Status> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void branchDelete(DevMachine devMachine,
                             ProjectConfigDto project,
                             String name,
                             boolean force,
                             AsyncRequestCallback<String> callback) {
        BranchDeleteRequest branchDeleteRequest = dtoFactory.createDto(BranchDeleteRequest.class).withName(name).withForce(force);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH_DELETE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchDeleteRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> branchDelete(DevMachine devMachine, Path project, String name, boolean force) {
        BranchDeleteRequest branchDeleteRequest = dtoFactory.createDto(BranchDeleteRequest.class).withName(name).withForce(force);
        String url = devMachine.getWsAgentBaseUrl() + BRANCH_DELETE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, branchDeleteRequest).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void branchRename(DevMachine devMachine,
                             ProjectConfigDto project,
                             String oldName,
                             String newName,
                             AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH_RENAME + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                           .send(callback);
    }

    @Override
    public Promise<Void> branchRename(DevMachine devMachine, Path project, String oldName, String newName) {
        String params = "?projectPath=" + project + "&oldName=" + oldName + "&newName=" + newName;
        String url = devMachine.getWsAgentBaseUrl() + BRANCH_RENAME + params;
        return asyncRequestFactory.createPostRequest(url, null).loader(loader)
                                  .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                                  .send();
    }

    /** {@inheritDoc} */
    @Override
    public void branchCreate(DevMachine devMachine, ProjectConfigDto project, String name, String startPoint,
                             AsyncRequestCallback<Branch> callback) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH_CREATE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchCreateRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    @Override
    public Promise<Branch> branchCreate(DevMachine devMachine, Path project, String name, String startPoint) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = devMachine.getWsAgentBaseUrl() + BRANCH_CREATE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, branchCreateRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Branch.class));
    }

    /** {@inheritDoc} */
    @Override
    public void checkout(DevMachine devMachine,
                         ProjectConfig project,
                         CheckoutRequest checkoutRequest,
                         AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, checkoutRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> checkout(DevMachine devMachine,
                                  Path project,
                                  CheckoutRequest request) {

        final String url = devMachine.getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(url, request).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void remove(DevMachine devMachine,
                       ProjectConfigDto project,
                       List<String> items,
                       boolean cached,
                       AsyncRequestCallback<String> callback) {
        RmRequest rmRequest = dtoFactory.createDto(RmRequest.class).withItems(items).withCached(cached).withRecursively(true);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOVE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, rmRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remove(DevMachine devMachine, Path project, Path[] items, boolean cached) {
        List<String> files = new ArrayList<>();

        if (items != null) {
            for (Path item : items) {
                files.add(item.isEmpty() ? "." : item.toString());
            }
        }

        RmRequest rmRequest = dtoFactory.createDto(RmRequest.class).withItems(files).withCached(cached).withRecursively(true);
        String url = devMachine.getWsAgentBaseUrl() + REMOVE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, rmRequest).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void reset(DevMachine devMachine,
                      ProjectConfigDto project,
                      String commit,
                      @Nullable ResetRequest.ResetType resetType,
                      @Nullable List<String> filePattern,
                      AsyncRequestCallback<Void> callback) {
        ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
        if (resetType != null) {
            resetRequest.setType(resetType);
        }
        if (filePattern != null) {
            resetRequest.setFilePattern(filePattern);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + RESET + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> reset(DevMachine devMachine, Path project, String commit, ResetRequest.ResetType resetType, Path[] files) {
        ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
        if (resetType != null) {
            resetRequest.setType(resetType);
        }
        if (files != null) {
            List<String> fileList = new ArrayList<>(files.length);
            for (Path file : files) {
                fileList.add(file.isEmpty() ? "." : file.toString());
            }
            resetRequest.setFilePattern(fileList);
        }
        String url = devMachine.getWsAgentBaseUrl() + RESET + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void log(DevMachine devMachine, ProjectConfigDto project, List<String> fileFilter, boolean isTextFormat,
                    @NotNull AsyncRequestCallback<LogResponse> callback) {
        LogRequest logRequest = dtoFactory.createDto(LogRequest.class).withFileFilter(fileFilter);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + LOG + "?projectPath=" + project.getPath();
        if (isTextFormat) {
            asyncRequestFactory.createPostRequest(url, logRequest).send(callback);
        } else {
            asyncRequestFactory.createPostRequest(url, logRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
        }
    }

    @Override
    public Promise<LogResponse> log(DevMachine devMachine, Path project, Path[] fileFilter, boolean plainText) {

        List<String> paths = null;

        if (!isNullOrEmpty(fileFilter)) {
            paths = new ArrayList<>(fileFilter.length);

            for (Path file : fileFilter) {
                paths.add(file.isEmpty() ? "." : file.toString());
            }
        }

        LogRequest logRequest = dtoFactory.createDto(LogRequest.class)
                                          .withFileFilter(paths);
        String url = devMachine.getWsAgentBaseUrl() + LOG + "?projectPath=" + project;
        if (plainText) {
            return asyncRequestFactory.createPostRequest(url, logRequest)
                                      .loader(loader)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        } else {
            return asyncRequestFactory.createPostRequest(url, logRequest)
                                      .loader(loader)
                                      .header(ACCEPT, APPLICATION_JSON)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remoteAdd(DevMachine devMachine,
                          ProjectConfig project,
                          String name,
                          String repositoryURL,
                          AsyncRequestCallback<String> callback) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(repositoryURL);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE_ADD + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, remoteAddRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteAdd(DevMachine devMachine, Path project, String name, String url) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(url);
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE_ADD + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(requestUrl, remoteAddRequest).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void remoteDelete(DevMachine devMachine,
                             ProjectConfig project,
                             String name,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE_DELETE + '/' + name + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, null).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteDelete(DevMachine devMachine, Path project, String name) {
        String url = devMachine.getWsAgentBaseUrl() + REMOTE_DELETE + '/' + name + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, null).loader(loader).send();
    }

    /** {@inheritDoc} */
    @Override
    public void fetch(DevMachine devMachine,
                      ProjectConfigDto project,
                      String remote,
                      List<String> refspec,
                      boolean removeDeletedRefs,
                      RequestCallback<String> callback) throws WebSocketException {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);

        String url = FETCH + "?projectPath=" + project.getPath();
        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(fetchRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> fetch(DevMachine devMachine, Path project, String remote, List<String> refspec, boolean removeDeletedRefs) {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);
        String url = devMachine.getWsAgentBaseUrl() + FETCH + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, fetchRequest).send();
    }

    /** {@inheritDoc} */
    @Override
    public void pull(DevMachine devMachine,
                     ProjectConfigDto project,
                     String refSpec,
                     String remote,
                     AsyncRequestCallback<PullResponse> callback) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PULL + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pullRequest).send(callback);
    }

    @Override
    public Promise<PullResponse> pull(DevMachine devMachine, Path project, String refSpec, String remote) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = devMachine.getWsAgentBaseUrl() + PULL + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, pullRequest).send(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class));
    }

    /** {@inheritDoc} */
    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffRequest.DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     String commitB, @NotNull AsyncRequestCallback<String> callback) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(fileFilter)
                                            .withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withCommitB(commitB)
                                            .withRenameLimit(renameLimit);

        diff(devMachine, diffRequest, project.getPath(), callback);
    }

    @Override
    public Promise<String> diff(DevMachine devMachine,
                                Path project,
                                List<String> fileFilter,
                                DiffRequest.DiffType type,
                                boolean noRenames,
                                int renameLimit,
                                String commitA,
                                String commitB) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(fileFilter)
                                            .withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withCommitB(commitB)
                                            .withRenameLimit(renameLimit);

        String url = devMachine.getWsAgentBaseUrl() + DIFF + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, diffRequest).loader(loader).send(new StringUnmarshaller());
    }

    /** {@inheritDoc} */
    @Override
    public void showFileContent(DevMachine devMachine,
                                @NotNull ProjectConfigDto project,
                                String file,
                                String version,
                                @NotNull AsyncRequestCallback<ShowFileContentResponse> callback) {
        ShowFileContentRequest showRequest = dtoFactory.createDto(ShowFileContentRequest.class).withFile(file).withVersion(version);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + SHOW + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, showRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<ShowFileContentResponse> showFileContent(DevMachine devMachine, Path project, Path file, String version) {
        ShowFileContentRequest showRequest = dtoFactory.createDto(ShowFileContentRequest.class)
                                                       .withFile(file.toString())
                                                       .withVersion(version);
        String url = devMachine.getWsAgentBaseUrl() + SHOW + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, showRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class));
    }

    /** {@inheritDoc} */
    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffRequest.DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     boolean cached,
                     AsyncRequestCallback<String> callback) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(fileFilter).withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withRenameLimit(renameLimit)
                                            .withCached(cached);

        diff(devMachine, diffRequest, project.getPath(), callback);
    }

    @Override
    public Promise<String> diff(DevMachine devMachine,
                                Path project,
                                List<String> files,
                                DiffRequest.DiffType type,
                                boolean noRenames,
                                int renameLimit,
                                String commitA,
                                boolean cached) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(files)
                                            .withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withRenameLimit(renameLimit)
                                            .withCached(cached);

        String url = devMachine.getWsAgentBaseUrl() + DIFF + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, diffRequest).loader(loader).send(new StringUnmarshaller());
    }

    /**
     * Make diff request.
     *
     * @param diffRequest
     *         request for diff
     * @param projectPath
     *         project path
     * @param callback
     *         callback
     */
    private void diff(DevMachine devMachine, DiffRequest diffRequest, @NotNull String projectPath, AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + DIFF + "?projectPath=" + projectPath;
        asyncRequestFactory.createPostRequest(url, diffRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void merge(DevMachine devMachine,
                      ProjectConfigDto project,
                      String commit,
                      AsyncRequestCallback<MergeResult> callback) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + MERGE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, mergeRequest).loader(loader)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public Promise<MergeResult> merge(DevMachine devMachine, Path project, String commit) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = devMachine.getWsAgentBaseUrl() + MERGE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, mergeRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MergeResult.class));
    }

    /** {@inheritDoc} */
    @Override
    public void getCommitters(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Commiters> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMITERS + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRepository(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Void> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + DELETE_REPOSITORY + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON).header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public Promise<Void> deleteRepository(DevMachine devMachine, final Path project) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                String url = DELETE_REPOSITORY + "?projectPath=" + project.toString();
                final Message message = new MessageBuilder(GET, url).header(CONTENTTYPE, APPLICATION_JSON)
                                                                    .header(ACCEPT, TEXT_PLAIN)
                                                                    .build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void getUrlVendorInfo(DevMachine devMachine, @NotNull String vcsUrl, @NotNull AsyncRequestCallback<GitUrlVendorInfo> callback) {
        asyncRequestFactory.createGetRequest(appContext.getDevMachine().getWsAgentBaseUrl() + "/git-service/info?vcsurl=" + vcsUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }
}