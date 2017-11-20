commit 82de508d727761a9d95ce506a23e39b372f82a93
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Wed Dec 23 17:48:03 2015 +0900

    ZEPPELIN-501 Notebook search

    ### What is this PR for?
    This PR has for goal to allow the user to search through the code in all the paragraphs *and notebook names* in all the notebooks

    It add a simple 'search bar' to the nav-bar of Zeppelin WebApp, and an in-memory fulltext search index of paragraphs to the backend.

    The search is pretty basic now, fine-tuning it for better search over all types of source code will be a subject of further work.

    ### What type of PR is it?
    Feature

    ### Todos
    * [x] - Fix typos :dancer: b853aa6
    * [x] - fix js issue in js console felizbear 29da337
    * [x] - update index on paragraph CRUD:
      - [x] Read (initial work)
      - [x] Create\Delete 825b266
      - [x] Update e915a69
      - [x] Delete paragraph
    * [x] - add license to zeppelin-distribution/src/bin_license/LICENSE (backend for lucene, not sure about angular-resource as it is part of the AngularJS codebase, but will add just in case) c00b516
    * [x] - add missing Apache headers ded9c3b felizbear 29da337
    * [x] - fix CI (~~was failing RAT on zengine Too many files with unapproved license: 2~~, now flacky integration test AKA [ZEPPELIN-510](https://issues.apache.org/jira/browse/ZEPPELIN-510))
    * [x] - index notebook names e80c3e5, felizbear 29da337
    * [x] - make NotebookRepoSync.sync() package private again  5a18bc8
    * [x] - NPE on persisting changes of existing notebook
    * [x] - reduce log verbosity

    ### Is there a relevant Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-501

    ### How should this be tested?
    Outline the steps to test the PR here.

    ### Screenshots (if appropriate)
    ![screen shot 2015-12-15 at 19 49 18](https://cloud.githubusercontent.com/assets/5582506/11808681/34d004e8-a365-11e5-9452-ea616029073c.png)

    ![screen shot 2015-12-15 at 19 52 03](https://cloud.githubusercontent.com/assets/5582506/11808699/5dc023ce-a365-11e5-953d-c23235446f9c.png)

    ### Questions:
    * Does the licenses files need update? Yes, added to license files
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Yes

    This work is a collaboration with felizbear who contributed major parts of the frontend changes.

    Author: Alexander Bezzubov <bzz@apache.org>
    Author: felizbear <ilya@nflabs.com>
    Author: Alexander <abezzubov@nflabs.com>

    Closes #534 from bzz/notebook-search and squashes the following commits:

    c7f1d35 [Alexander Bezzubov] ZEPPELIN-501: refactoring, extract SearchService interface + Lucen-based impl
    e3af25c [Alexander Bezzubov] ZEPPELIN-501: muting more search logs info->debug
    c77d53b [Alexander Bezzubov] ZEPPELIN-501: fix + test for bug in index key structure
    78f69a3 [Alexander Bezzubov] ZEPPELIN-501: update index on paragraph delete
    73a28bb [Alexander Bezzubov] ZEPPELIN-501: fix NPE on Note creation by NotebookRepo, \wo DI constructor
    cf44b1f [Alexander Bezzubov] ZEPPELIN-501: fix NPE on double-delete
    d2b44ac [Alexander Bezzubov] ZEPPELIN-501: stop printing whole note in logs
    f30e6c8 [Alexander Bezzubov] Merge branch 'master' into notebook-search
    da69c07 [Alexander Bezzubov] ZEPPELIN-501: validate notes before update index
    5f47890 [Alexander Bezzubov] ZEPPELIN-501: fix bug (merge artefact) that failed CI befor
    e915a69 [Alexander Bezzubov] ZEPPELIN-501: update notebooks on save + tests
    3f20904 [Alexander Bezzubov] ZEPPELIN-501: refactoring, compiler warning in Paragraph.java
    40cf9e8 [Alexander Bezzubov] ZEPPELIN-501: refactoring, cleaning compiler warnings in Notebook.java
    6a3906f [Alexander Bezzubov] Merge branch 'master' into notebook-search
    2b2f8dc [Alexander Bezzubov] Merge branch 'master' into notebook-search
    44235eb [Alexander Bezzubov] ZEPPELIN-501: refactoring, cleaning compiler warnings in Note.java
    eb7878a [Alexander Bezzubov] Merge branch 'master' into notebook-search
    0ce8a92 [Alexander Bezzubov] ZEPPELIN-501: add missing logger
    6da1dc9 [Alexander Bezzubov] Merge branch 'master' into notebook-search
    8c0f29a [Alexander Bezzubov] ZEPPELIN-501: fixing NPE in tests \w mocks
    825b266 [Alexander Bezzubov] ZEPPELIN-501: refactoring + handling index Create\Delete
    b13d5fb [Alexander Bezzubov] Merge branch 'master' into notebook-search
    009b290 [Alexander Bezzubov] ZEPPELIN-501: muting logs back
    0efc00e [Alexander Bezzubov] Merge branch 'master' into notebook-search
    b0b2c54 [Alexander Bezzubov] ZEPPELIN-501: refactoring, renames + verbose logging ON
    36b2467 [Alexander Bezzubov] ZEPPELIN-501: add default val for Note.name
    c7ae983 [Alexander Bezzubov] Merge branch 'master' into notebook-search
    ded9c3b [Alexander Bezzubov] ZEPPELIN-501: adding missing license headers
    cd2173e [Alexander Bezzubov] ZEPPELIN-501: tixing fypo in docs, again :see_no_evil:
    1952847 [Alexander Bezzubov] ZEPPELIN-501: minor test update
    6180c86 [Alexander Bezzubov] ZEPPELIN-501: mute logs, invalid notes do not fail all indexing
    29da337 [Alexander] Merge pull request #7 from felizbear/search-fix
    c00b516 [Alexander Bezzubov] ZEPPELIN-501: update LICENCE file with new deps
    5a18bc8 [Alexander Bezzubov] ZEPPELIN-501: restore NotebookRepoSync.sync() visibility + compiler warning cleanup
    fcbff3d [felizbear] add missing apache license info to a source file
    00f0315 [felizbear] handle notebooks is search results
    7d06686 [felizbear] fix search-related bug in notebook controller
    e80c3e5 [Alexander Bezzubov] ZEPPELIN-501: Indexing notebook names
    b853aa6 [Alexander Bezzubov] ZEPPELIN-501: fixing typos in docs
    82c7dd7 [Alexander Bezzubov] ZEPPELIN-501: reverting accidental changes
    71ec51f [felizbear] clear search field on navigation to home view (/)
    1e1357c [felizbear] redirect to notebook and scroll to paragraph from search view
    09d44d2 [Alexander Bezzubov] Search: re-index note on every change
    b2b93c4 [Alexander Bezzubov] Search: refatoring, move SearchService from NotebookRepoSync -> Notebook
    08fe806 [Alexander Bezzubov] Search: adding tests
    63a4e05 [Alexander Bezzubov] Search: adding search API docs
    98f4e59 [Alexander Bezzubov] Search: refactoring, rename fragment -> snippet
    5c1e3e4 [Alexander Bezzubov] Refatoring - removing old annotations
    e363ed4 [Alexander Bezzubov] Search: make jshint happy
    7aad5cf [Alexander Bezzubov] Search: make checkstyle happy
    6ccd6f1 [Alexander Bezzubov] Search: nuke compiler warnings in NotebookRestApi
    aa5ddb3 [felizbear] center search results on screen
    227c6b4 [felizbear] update search results view: add panels for results
    865925c [felizbear] highlight syntax and search terms in search results
    9ca8628 [Alexander Bezzubov] Search: merge first 3 fragments + full paragraph in search result
    11127f0 [felizbear] style search widget for notebook search
    db246fa [Alexander Bezzubov] Search: highlighting added using fragments
    c2c2a52 [Alexander Bezzubov] Search: backend indexing using Lucene added to zengine
    163a465 [Alexander Bezzubov] Search: add search/result-list and switch to
    bc2458a [Alexander Bezzubov] Get rid of compiler warnings
    3900b60 [Alexander Bezzubov] Search: disabling UI on disconnect
    7880237 [Alexander Bezzubov] Search: backend REST API scetch /notebooks/search?q=... added
    c5928f9 [Alexander Bezzubov] Search: form added to navbar in frontend