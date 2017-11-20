commit ff197d06227001e671e7fa06deca7a8a64813fef
Author: Khalid Huseynov <khalidhnv@nflabs.com>
Date:   Tue Jun 14 11:18:22 2016 -0700

    Update and refactor NotebookRepo versioning API

    ### What is this PR for?
    This is firstly to refactor API for versioning and keep everthing inside of one interface (NotebookRepo) instead of two different interfaces (NotebookRepoVersioned). Secondly, there're modifications to existing versioning api, with considerations of future complete implementation of versioning. Note that this PR doesn't implement all suggested interfaces, but lays foundation for their implementation.

    ### What type of PR is it?
    Improvement && Refactoring

    ### Todos
    * [x] - move versioning api (get, history) from NotebookRepoVersioned to NotebookRepo
    * [x] - refactor and naming changes
    * [x] - modify checkpoint api (add return value) and modify NotebookRepoSync to deal with it

    ### What is the Jira issue?

    ### How should this be tested?
    Basically it doesn't add new functionality, so the only requirement is for tests to pass.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? not breaking, but some api changes
    * Does this needs documentation? No

    Author: Khalid Huseynov <khalidhnv@nflabs.com>

    Closes #1007 from khalidhuseynov/repo/versioning-api-update and squashes the following commits:

    f900058 [Khalid Huseynov] Rev -> Revision
    17eee08 [Khalid Huseynov] fix checkstyle
    9140b16 [Khalid Huseynov] fix tests
    ea46851 [Khalid Huseynov] apply changes to NotebookRepoSync
    e82d8a9 [Khalid Huseynov] propagate changes to all repos
    b68dd26 [Khalid Huseynov] move and update versioning api