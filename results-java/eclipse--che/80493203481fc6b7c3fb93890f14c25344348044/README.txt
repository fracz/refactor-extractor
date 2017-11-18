commit 80493203481fc6b7c3fb93890f14c25344348044
Author: Sun Seng David TAN <sutan@redhat.com>
Date:   Tue Jul 4 18:11:32 2017 +0200

    Openshift connector improvements (#5052)

    * CHE-4141 - Use Persistent Volumes Claims when creating workspaces

    Signed-off-by: Snjezana Peco <snjezana.peco@redhat.com>

    * Implement getContainerLogs method in OpenShiftConnector

    Signed-off-by: Snjezana Peco <snjezana.peco@redhat.com>

    * Implement createExec() and startExec() in OpenShiftConnector

    Add implementations of createExec() and startExec(). Since OpenShift
    does not separate the create and start steps, a holder class
    KubernetesExecHolder is necessary, to pass information between
    the call to createExec() (which just saves relevant information)
    and startExec().

    Additionally, adds KubernetesOutputAdapter, which parses the output
    from OpenShift into LogMessages that can be handled by Che's
    MessageProcessor<LogMessage> class.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Add implementation of getEvents() to avoid busy wait

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Update Dockerfile to avoid permissions issues

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * Che server and workpaces exposed on the same single TCP port (#4351)

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * Disabling usage of user account service in openshift-connector

    Signed-off-by: Sun Seng David Tan <sutan@redhat.com>

    * Update Docker Compose tests to fix test failure

    Updating to Jackson 2.7.7 causes tests in the docker compose
    plugin to fail. This is due to the fact that the tests expect
    empty values in dictionaries to be parsed as the empty string,
    whereas jackson 2.7.7 parses them as null (as specified by the
    yaml spec).

    Modifies the affected tests to explicitly use an empty string
    (i.e. "") instead of an empty value.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Find an alternative to subPath in volumeMount

    Signed-off-by: Snjezana Peco <snjezana.peco@redhat.com>

    * Setting rwx permissions for all on /data/ in case it's not mounted

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * Add support for resource limits when running on Openshift

    Add resource limits to workspace Pods when running on OpenShift.
    The memory limit is normally obtained from the API request to
    create the workspace, however it can be overridden via the property
    `che.openshift.workspace.memory.override`. The cpu limit used is
    determined by the property `che.openshift.workspace.cpu.limit`.

    In both cases, the value of the property is passed directly to
    OpenShift, so any valid quantity is acceptable (e.g. 150Mi,
    1Gi, 1024, etc).

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Fix dockerImageConfig is null (since v1.5 of OpenShift API)

    Signed-off-by: Sun Seng David Tan <sutan@redhat.com>

    * Add Nullable annotation to che.docker.ip.external

    The property che.docker.ip.external can be null, but
    OpenShiftConnector does not include the annotation. This
    prevents Che from initialising if e.g. running on docker
    without the property set.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * CHE-158 Adding TLS support for Workspace routes

    Signed-off-by: Ilya Buziuk <ibuziuk@redhat.com>

    * Adding property to set requests for RAM

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * CHE-158 Using '-' instead of '.' for generating OpenShift route Urls

    Signed-off-by: Ilya Buziuk <ibuziuk@redhat.com>

    * Fixing tests after changing Url generation logic

    Signed-off-by: Ilya Buziuk <ibuziuk@redhat.com>

    * Redirect insecure HTTP requests to TLS endpoint

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * CHE-180: Creating and closing OpenShiftClient in every method of OpenshiftConnector

    Signed-off-by: Ilya Buziuk <ibuziuk@redhat.com>

    * Update route naming to make it work on OSO

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * Rework PVC management on OpenShift

    - Change how subdirectories are created in pods to
      use a short, terminating job instead of a full deployment
    - Add OpenShiftWorkspaceFilesCleaner class to properly
      notice workspace deleted events
    - Add helper class to manage job pods. For creation, some
      effort is made to avoid attempting to create workspaces
      unnecessarily, but only exists in-memory
    - Workspace deletions are batched together so that removing
      workspaces directories can be done when server is idled,
      avoiding unnecessary PVC mounts
    - Add two new properties: che.openshift.jobs.image and
      che.openshift.jobs.memorylimit, which are used by
      OpenShiftPvcHelper to set up pods

    Current issues:
    - Since workspace directories are not deleted immediately,
      attempting to re-create a workspace with the same name
      will result in the previous instance's project to already
      be there. This should have a minor impact.
    - Memory for which workspace dirs have been created is not
      persisted, resulting in potentially unnecessary jobs
    - Openshift workspace files cleaner is included by overwriting
      binding in WsMasterModule instead of using a provider. This
      could be better, but OpenShift integration may be reaching a
      point where a custom module is a better solution.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>
    Signed-off-by: Sun Seng David Tan <sutan@redhat.com>

    * Delete ReplicaSets explicitly when shutting down a workspace

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Fix OpenShiftConnectorTest

    Signed-off-by: Mario Loriedo <mloriedo@redhat.com>

    * Fix route server names if unknown should start with server-.

    https://issues.jboss.org/browse/CHE-230

    Signed-off-by: Sun Seng David Tan <sutan@redhat.com>

    * Add property to control manual workspace dir creation in OpenShift

    Add property 'che.openshift.precreate.workspace.dirs'. If property is
    true, OpenShiftConnector will run a pod before launching workspaces
    to create a subpath in the workspace's persistent volume with correct
    permissions. If the property is false, this step is skipped.

    This is necessary as in older versions of OpenShift/Kubernetes, subpaths
    created as part of a volume mount are created with root permissions, and
    so cannot be modified by workspace pods. More recent versions fix this,
    creating subpath volumes with correct permissions, making the step above
    unnecessary.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * CHE-102 - Idle detection of che-server and workspaces

    Signed-off-by: Snjezana Peco <snjezana.peco@redhat.com>

    * Add and modify tests for OpenShift helper classes

    Add tests for the untested classes in openshift.client.kuberentes,
    and update existing tests where necessary.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>

    * Recent changes required access to `/` which is impossible under OS

    Signed-off-by: David Festal <dfestal@redhat.com>

    * adapt che-server entrypoint.sh to environments without write permissions in '/' (#5344)

    * adapt che-server entrypoint.sh to environments without write permissions in '/'

    * CHE-280: Adding container's state info to the 'inspectContainer' API

    Signed-off-by: Ilya Buziuk <ibuziuk@redhat.com>

    * Factorize code of `ServerEvaluationStrategy` classes, to use the Custom strategy as the basis of other strategies (#5366)

    * Pull-up the local docker port management (use exposed ports)

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Make all the strategies extend `CustomEvaluationStrategy`

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Add a `workspaceIdWithoutPrefix` macro and use it for `single-port`

    This macro is based on the `workspaceId` macro, but without the
    `workspace` prefix.

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Add the `isDevMachine` to allow conditions in the ST template.

    This is required to allow the `single-port` strategy to have a different
    url according to the type of machine. (see the work done for CHE-175 :
    Support multi-container workspaces on OpenShift)

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Small fixes after comments from @fbenoit

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Fix unnecessary space pointed out by @sunix

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Remove unnecessary `else` as suggested by @sunix

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Keep the method signatures compatible with the `condenvy` strategy

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Align names of parameters of constructors (requested by @garagatyi)

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Add a default implementation to avoid breaking the Codenvy build

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Also rename the attributes

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Use a constant for the `workspace` prefix string

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Fix formatting as requested by @sunix

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Use a constant for the `isDevMachine` macro name

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Add unit tests for `workspaceIdWithoutPrefixè and `isDevMachine` macros

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Another requested formatting fix

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Make new tests clearer

    Signed-off-by: David Festal <dfestal@redhat.com>

    * yet another formatting request

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Respect the original order of imports

    Signed-off-by: David Festal <dfestal@redhat.com>

    * remove unnecessary `toString()`

    Signed-off-by: David Festal <dfestal@redhat.com>

    * use a lowercase `S` in the `server-` prefix

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Multi-container workspace Support (#5110)

    * Fix 2 NPE that prevented using *non-dev* additional machines

    In the context of https://issues.jboss.org/browse/CHE-175

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Name openshift resources based on the machine name for non-dev machines

    This fixes https://issues.jboss.org/browse/CHE-259
    and https://issues.jboss.org/browse/CHE-258

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Fix failing Traeffik tests...

    ... by:
    - adding the new `CHE_IS_DEV_MACHINE` env variable in tests
    - pulling up all the `CustomServerEvaluationStrategy` features in an
    abstract `BaseServerEvaluationStrategy` (which all other Che strategies
    extend) and have the `CustomServerEvaluationStrategy` class simply
    extend this `BaseServerEvaluationStrategy`.

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Fix tests in the LocalDockerEvaluationStrategy...

    ... by correctly using the boolean attribute to manage the new use-case
    introduced by @fbenoit in master.

    Signed-off-by: David Festal <dfestal@redhat.com>

    * Replace OSIO-specific `single-port` strategy by `docker-local-custom`

    This fixes redhat-developer/rh-che#113

    Signed-off-by: David Festal <dfestal@redhat.com>