commit 8cde5c9bd47e3e9682d016274a808e096ab34d0b
Author: Khalid Huseynov <khalidhnv@nflabs.com>
Date:   Sat May 28 11:43:07 2016 -0700

    ZeppelinHub notebook storage/connection repository

    ### What is this PR for?
    This is to add [ZeppelinHub](https://www.zeppelinhub.com) notebook storage/connection layer to the Zeppelin.

    ### What type of PR is it?
    Feature

    ### Todos
    * [x] - NotebookRepo rest api
    * [x] - ZeppelinHub websocket client
    * [x] - Zeppelin websocket client
    * [x] - Tests
    * [x] - More QA (authentication consistency, etc.)
    * [x] - Address review comments

    ### What is the Jira issue?

    ### How should this be tested?
    First of all, you may need to create account in [ZeppelinHub](https://www.zeppelinhub.com).
    Then you can set connection by following guides in [here](https://github.com/khalidhuseynov/incubator-zeppelin/blob/feat/zeppelinhub-storage/docs/storage/storage.md#notebook-storage-in-zeppelinhub--).
    Finally you should be able to access and manipulate your notebooks from inside of your ZeppelinHub account.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Yes

    Author: Khalid Huseynov <khalidhnv@nflabs.com>
    Author: Anthony Corbacho <corbacho.anthony@gmail.com>

    Closes #880 from khalidhuseynov/feat/zeppelinhub-storage and squashes the following commits:

    b480176 [Anthony Corbacho] Change debug log from info to error
    db8d1e8 [Anthony Corbacho] Remove spave after comment
    1c78d8c [Khalid Huseynov] address @bzz comments
    b1fe8a3 [Khalid Huseynov] move jetty.version property to root
    acf58d5 [Khalid Huseynov] address @AhyoungRyu comments
    8de3d1f [Khalid Huseynov] add zeppelin  hearbeat
    6c3aff2 [Khalid Huseynov] remove 074098eeb8ace6545c159d26657768079ae4b208 and ZeppelinHubConnection
    efcfca3 [Khalid Huseynov] more docs
    28f6bf7 [Khalid Huseynov] add authentication to zeppelin
    1ca3d65 [Khalid Huseynov] scheduler service as singleton
    32497dc [Anthony Corbacho] When user run a notebook, check if the websocket session exist, open it if close
    36176ea [Anthony Corbacho] change log info to debug
    8ad482d [Anthony Corbacho] Add routine to check ws connection to zeppelinhub is open, if not open it (keep trying)
    27a4042 [Anthony Corbacho] rename fct hub -> ZeppelinHub
    4c47e43 [Anthony Corbacho] code cleanup & walkthrough
    636b9fa [Khalid Huseynov] fix tests
    4b14a98 [Anthony Corbacho] add ASF headers
    4ac8115 [Khalid Huseynov] add config info and docs
    18ee802 [Khalid Huseynov] improve  repo tests
    2d27ec6 [Khalid Huseynov] add zeppelinhub ws client test
    ebcf692 [Khalid Huseynov] add zeppelin client tests
    9f1b8bf [Khalid Huseynov] add zeppelin websocket client
    1ce01ef [Khalid Huseynov] add rest package
    45bec47 [Anthony Corbacho] add zeppelinhub websocket client
    f8f168a [Anthony Corbacho] Add zeppelinhubmessage class + tests
    a1c5f52 [Anthony Corbacho] prerequisite changes for starting websocket client
    45ed9e1 [Khalid Huseynov] initial rest api