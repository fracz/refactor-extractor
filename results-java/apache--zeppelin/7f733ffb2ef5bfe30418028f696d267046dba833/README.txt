commit 7f733ffb2ef5bfe30418028f696d267046dba833
Author: Anthony Corbacho <corbacho.anthony@gmail.com>
Date:   Tue Aug 23 11:40:09 2016 +0900

    Zeppelin 1307 - Implement notebook revision in Zeppelinhub repo

    ### What is this PR for?
    Implement versioning in ZeppelinHub notebook storage.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - Implement Versioning API

    ### What is the Jira issue?
     * [ZEPPELIN-1307](https://issues.apache.org/jira/browse/ZEPPELIN-1307)

    ### How should this be tested?
    Edit `zeppelin-env.sh` and add `org.apache.zeppelin.notebook.repo.zeppelinhub.ZeppelinHubRepo` in `ZEPPELIN_NOTEBOOK_STORAGE`.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? NO
    * Is there breaking changes for older versions? NO
    * Does this needs documentation? NO

    Author: Anthony Corbacho <corbacho.anthony@gmail.com>

    Closes #1338 from anthonycorbacho/ZEPPELIN-1307 and squashes the following commits:

    dd57e7f [Anthony Corbacho] Fix NPE
    aef5cf3 [Anthony Corbacho] cleanup code
    6cd9251 [Anthony Corbacho] revert change to try ressource stmnt
    3b919a9 [Anthony Corbacho] Rework log trace
    74a0cdb [Anthony Corbacho] change asyncPutWithResponseBody to accpet url instead of noteId
    2395a6e [Anthony Corbacho] Light refactor of ZeppelinHubRestapiHandler and extract api call to a single method
    5d4b54b [Anthony Corbacho] Implement checkpoint method
    3942a78 [Anthony Corbacho] Implement get revision
    9bd0946 [Anthony Corbacho] Close InputStream in asyncGet