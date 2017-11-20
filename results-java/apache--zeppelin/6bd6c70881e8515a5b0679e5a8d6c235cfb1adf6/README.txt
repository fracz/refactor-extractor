commit 6bd6c70881e8515a5b0679e5a8d6c235cfb1adf6
Author: 1ambda <1amb4a@gmail.com>
Date:   Mon Jul 10 15:52:37 2017 +0900

    [ZEPPELIN-2749] Use scalable file structure for zeppelin web

    ### What is this PR for?

    We have improved zeppelin-web, but some parts are still messy. As part of keeping zeppelin-web module healthy ([ZEPPELIN-2725](https://issues.apache.org/jira/browse/ZEPPELIN-2725)), I suggest having these file structure. (Refer the screenshot section)

    Here are few reasons.

    - unified directory, file name helps us to recognize, find which part we should modify / fix
      * Let's say we need to modify the resize feature of paragraph, where the developer can find it? currently, it's under `component/resizable` not under `paragraph/resizeable` and also it's not the shareable component. There is no reason to keep that files in `component/**`
    - [this structure](https://github.com/toddmotto/angularjs-styleguide#file-naming-conventions) is what the angularjs community has verified for few years. so newly joined developers can feel more comfortable.
    - this is necessary for [Modular archiecture](https://issues.apache.org/jira/browse/ZEPPELIN-2750) and it eventually helps us to make a smooth transition toward next technologies (even whatever we will use)

    Additionally,

    - This is not the meaningless refactoring PR and doesn't block developing new features / fixes (Please refer the `Some Details` section)
    - I will handle conflicts for few days would be brought by other WIPs

    For your information,

    - https://github.com/toddmotto/angularjs-styleguide#file-naming-conventions
    - https://github.com/johnpapa/angular-styleguide/blob/master/a1/README.md#naming

    #### How to Review This PR?

    Please follow the commits. I modified submodules by splitting commits. Thus commit message includes what has been done in that PR. For example,

    ![image](https://user-images.githubusercontent.com/4968473/27993114-d8ac45e6-64dd-11e7-8130-f3fa887054a1.png)

    #### Some Details

    - Didn't change the widely used variable names not to make many conflicts. For example, `websocketMsgSrv`, `arrayOrderingSrv`
    - Since there are helium packages already published, didn't change the HTML file names like `pivot_setting.html` (it's better to use `pivot-setting.html` if we following the rule)

    ### What type of PR is it?
    [Improvement | Refactoring]

    ### Todos

    Please refer the commit message.

    ### What is the Jira issue?

    [ZEPPELIN-2749](https://issues.apache.org/jira/browse/ZEPPELIN-2749)

    ### How should this be tested?

    **All functionalities must work** as like before, CI will test it.

    ### Screenshots (if appropriate)

    #### Before: messy, mixed directory structure

    ![image](https://user-images.githubusercontent.com/4968473/27993126-0a94aca6-64de-11e7-93db-548b6fcc6913.png)

    #### After: only the shared components will be placed under `components/`

    ![image](https://user-images.githubusercontent.com/4968473/27993118-ee1bd2d4-64dd-11e7-95b6-f71dc628a94e.png)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #2472 from 1ambda/ZEPPELIN-2749/use-scalable-file-structure-for-zeppelin-web and squashes the following commits:

    22e6bf95 [1ambda] fix: AuthenticationIT, InterpreterModeActionsIT
    e31f0bc8 [1ambda] fix: AbstractZeppelinIT
    102e5443 [1ambda] paragraph-parameterized-query-form: Rename
    74e0af41 [1ambda] paragraph-progress-bar: Rename
    0029d6b7 [1ambda] job-progress-bar: Rename
    af420a41 [1ambda] helium: Move helium related files into app/helium
    285a6462 [1ambda] notebook-repository: Rename injected ctrl name
    147572f1 [1ambda] notebook-repository: Rename files, funcs
    4f66b642 [1ambda] ng-*: Rename files, funcs
    f25d981e [1ambda] interpreter: Move interpreter specific directives into interpreter/
    4a0602f7 [1ambda] array-ordering: Rename files
    1e4ba709 [1ambda] code-editor: Move paragraph specific directive into paragraph/code-editor
    31dcf6a3 [1ambda] base-url: Rename file
    1afb2d03 [1ambda] note-name-filter: Rename files
    ec046683 [1ambda] dropdown-input: Move notebook specific directive into notebook/dropdown-input
    81fa2d44 [1ambda] elastic-input: Move notebook specific ctrl into notebook/elastic-input
    657d0638 [1ambda] note-rename: Add prefix note-
    47ac45d1 [1ambda] expand-collapse: Move navbar specific directive into navbar/
    8bc78124 [1ambda] login: Remove invalid attr in login
    b2bf91b0 [1ambda] note-import: Rename to noteImportCtrl
    07b1f3ff [1ambda] note-create: Rename to noteCreateModal
    568149ed [1ambda] note-create: Rename injected controller
    c3402449 [1ambda] note-create: Rename files
    e17c7ee7 [1ambda] note-create: Remove useless dialog postfix
    b0a36a7e [1ambda] note-import: Remove meaningless postfix dialog
    abf6869d [1ambda] note-import: Rename files
    a40ea23c [1ambda] search: Move search specific service into serach/
    7a094841 [1ambda] browser-detect: move save-as specific service into save-as/
    40f62e3c [1ambda] rename: Modify injected service name
    e993133a [1ambda] rename: Rename funcs
    1f950943 [1ambda] note-action: Rename injected service name
    d3da2d45 [1ambda] note-action: Rename files, funcs
    b9f3c178 [1ambda] note-list-elem: Rename
    d3132e9a [1ambda] note-list: Remove useless middle word Data
    b5dff142 [1ambda] resizable: Move para specific directive into paragraph/
    bfcd6b85 [1ambda] resizable: Rename funcs
    40643997 [1ambda] fix: Remove useless postfix and rename files, funcs
    0611aff5 [1ambda] websocket: Rename funcs
    a2527530 [1ambda] fix: Remove meaningless postfix from searchService
    c21a1237 [1ambda] move: paragraph specific ctrl into paragraph/
    ad99c04b [1ambda] move: note specific dialog into notebook/
    701f4432 [1ambda] move: note specific service into notebook/
    3f02c503 [1ambda] fix: Remove unused saveAsService from paragraph ctrl
    d7d7434a [1ambda] move: note specific service saveAsService into notebook/
    4e7f6d9c [1ambda] fix: Move repository-create into interpreter/
    a9a6368a [1ambda] fix: remove useless dir interpreter-create
    c1c210de [1ambda] rename: repository-dialog -> repository-create