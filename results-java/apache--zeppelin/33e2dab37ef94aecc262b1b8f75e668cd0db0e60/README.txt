commit 33e2dab37ef94aecc262b1b8f75e668cd0db0e60
Author: Anthony Corbacho <corbacho.anthony@gmail.com>
Date:   Wed Nov 23 14:43:59 2016 +0900

    [ZEPPELIN-1690] - ZeppelinHubNotebookRepo multy user handling

    ### What is this PR for?
     This PR bring multi user handling to ZeppelinHubNotebookRepo.

    ### What type of PR is it?
    [Improvement ]

    ### What is the Jira issue?
     * [ZEPPELIN-1690](https://issues.apache.org/jira/browse/ZEPPELIN-1690)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Anthony Corbacho <corbacho.anthony@gmail.com>

    Closes #1635 from anthonycorbacho/feat/ZeppelinHubRepoMultiUser and squashes the following commits:

    d9e0036 [Anthony Corbacho] Move code location
    d8989aa [Anthony Corbacho] Handle invalid subject
    aef6e00 [Anthony Corbacho] Fix check style
    edb9e8c [Anthony Corbacho] Desactivate ws :: we will need to refactor this part
    e884203 [Anthony Corbacho] Refactor :: remove 'async' on every http call
    dbb8ebd [Anthony Corbacho] Fix test
    25f6215 [Anthony Corbacho] pass user token to zeppelinhub rest api handler
    674fb93 [Anthony Corbacho] Refactor ZeppelinHub rest API handler  - Now takes a token on every http request
    3fbfcfa [Anthony Corbacho] Add new login on how user can get his token at runtime
    a8aeb51 [Anthony Corbacho] add comment in zeppelinhubRealm about saving user session in a singleton map
    5931ab6 [Anthony Corbacho] Fix check style
    67051a0 [Anthony Corbacho] Add ZeppelinHub instance model
    e3e5a15 [Anthony Corbacho] Add userTiket in AuthenticationInfo on OnMessage method in notebookServer
    7a0c959 [Anthony Corbacho] Add zeppelinhub user session to userSession container after login throght zeppelinhubRealm
    0729f51 [Anthony Corbacho] Add zeppelinhub session container