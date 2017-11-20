commit 5e85e6ea6ff004735fde9bef58085b83369f864b
Author: Anthony Corbacho <corbacho.anthony@gmail.com>
Date:   Mon Nov 14 14:56:38 2016 +0900

    [ZEPPELIN-1610] - Add notebook watcher

    ### What is this PR for?
    Add a Simple way to switch a websocket connection to a new state; watcher.
    A websocket watcher is a special connection that will watch most of the web socket even in Zeppelin, this cam be used to monitor zeppelin server activity.

    ### What type of PR is it?
    [Feature]

    ### Todos
    * [x] - Add watcher Queue
    * [x] - Add endpoint to switch from regular client to watcher
    * [x] - Add a way to generate a uniq key when zeppelin server restart
    * [x] - Add example on how to use watcher.

    ### What is the Jira issue?
    * [ZEPPELIN-1610](https://issues.apache.org/jira/browse/ZEPPELIN-1610)

    ### How should this be tested?
    You will have to create your own websocket client and provide a valid http header (`X-Watcher-Key`) when you connect to zeppelin ws
     something like
    ```
    private Session openWatcherSession() {
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader(WatcherSecurityKey.HTTP_HEADER, WatcherSecurityKey.getKey());
        WatcherWebsocket socket = WatcherWebsocket.createInstace();
        Future<Session> future = null;
        Session session = null;
        try {
          future = wsClient.connect(socket, zeppelinWebsocketUrl, request);
          session = future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
          LOG.error("Couldn't establish websocket connection to Zeppelin ", e);
          return session;
        }
        return session;
      }
    ```

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Yes

    Author: Anthony Corbacho <corbacho.anthony@gmail.com>

    Closes #1588 from anthonycorbacho/feat/updateWebsocketInZeppelinHubRepo and squashes the following commits:

    26cf53f [Anthony Corbacho] Move broadcastToWatcher
    8413e9b [Anthony Corbacho] Remove redundant broadcast
    fb2c260 [Anthony Corbacho] Remove TODO in socket/Message
    716c92c [Anthony Corbacho] Fix checkstyle
    a10ba13 [Anthony Corbacho] Add remove connection from note back in test
    89d70f2 [Anthony Corbacho] fix test
    092791e [Anthony Corbacho] Light refactoring :: add missing header, add comment, refacto some methods
    8f7e1b3 [Anthony Corbacho] Add X-Watcher-Key in request header for watcher client
    e2d3053 [Anthony Corbacho] Add simple check for ws before switching ws client to watcher, client should provide a header X-Watcher-Key with a valid key (generated at runtime), if key invalid client wont be accepted
    e25ea1e [Anthony Corbacho] Add simple Key generation for Watcher ws client
    4affe25 [Anthony Corbacho] Handle remoing wssession from notebook map once the session is close :: avoiding socket connection to be ide
    c32192a [Anthony Corbacho] rework watcher creation and ws session with notes
    3bd3482 [Anthony Corbacho] Reorder import :: Google check style
    bde5db4 [Anthony Corbacho] Update ping routine
    ede1f18 [Anthony Corbacho] make private field public for accessibility
    aa55a5a [Anthony Corbacho] Strting to rework ZeppelinClient
    e5b3a1d [Anthony Corbacho] Add zeppelinhub notebook watcher
    9d6c93f [Anthony Corbacho] Add new OP watcher
    0d7f493 [Anthony Corbacho] Added new WS queue called watcher, watcher will be abler to listen to almost every note action performed in zeppelin notebook websocket server
    45849ce [Anthony Corbacho] Add new message type :: Watcher message, this class will wrapp zeppelin ws message and add extra information such as noteId and user