commit b66bddc26b130e15e46f9cb7a838b01d28014945
Author: Zabil Cheriya Maliackal <zabil@users.noreply.github.com>
Date:   Mon Dec 12 16:33:33 2016 +0530

    Refactor agent controller to switch between http and websockets (#2745) (#2864)

    * Refactor agent controller to switch between http and websockets (#2745)

    * Introduce a factory method for initializing the agent controller.
    Pull methods up from the Agent service to controller.

    * Check if websockets are turned on and return the web socker controller.
    Use the new spring scheduling framework.

    * Add a new connection builder for Web Sockets.
    Initialize the web socket with custom trust all settings to get around jetty's limitation of not allowing trust all when an agent store is specified. GoCD requires the agent to be registered to communicate and this requires passing the agent certificates while connection to the web socket end points.

    * Rename ack -> acknowledgementId for readability

    * Modify the message to always have acknowledgment.
    Split out the session handling and web socket client handling.
    Write unit tests for session and client connection logic.
    Reuse client upgrade and security exception handling.

    * Added log messages when messages are acknowledged or cleared.ˆ

    * Fixed header formatting

    * Remove anonymous authentication for websockets

    * Check the localhost name and the ip address only when the controller is created.
    Remove synchronized when setting the session.

    * Avoid flaky test by handling message end point using a fake remote end point

    * Ignore acknowledgment messages by default on test stub

    * Fix the copyright headers

    * Fix tests to use the right http builder

    * Remove unused delay properties

    * Ignore hostname while verifying certificates.
    Set the root cert file only when specified.

    * Create (and stop) the websocket client, only if it is not already running

    * Moving controller creation to a factory class so that we do not have to explicitly pass constructor arguments from applicationContext.xml