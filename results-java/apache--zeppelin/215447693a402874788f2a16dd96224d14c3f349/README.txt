commit 215447693a402874788f2a16dd96224d14c3f349
Author: Khalid Huseynov <khalidhnv@nflabs.com>
Date:   Thu May 19 11:42:43 2016 +0900

    Refactor CORS filter into webapp context handler

    ### What is this PR for?
    #867 was a hotfix and merged immediately. However we can refactor CORS filter into more appropriate place, which is webapp context handler instead of keeping it in rest api handler (since it also applies to websocket api).

    ### What type of PR is it?
    Refactoring

    ### Todos
    * [x] - move from rest to webapp

    ### What is the Jira issue?

    ### How should this be tested?
    Basically this is refactoring, and original flow holds.
    For testing, can use same routine as in #867.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Khalid Huseynov <khalidhnv@nflabs.com>

    Closes #900 from khalidhuseynov/fix/refactor-cors-filter and squashes the following commits:

    210a613 [Khalid Huseynov] move cors filter from rest api to webapp handler