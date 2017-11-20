commit ba87ad33439e53c14741b2c23e9dc2b7a3813b2a
Author: felizbear <ilya@nflabs.com>
Date:   Mon Dec 5 20:17:57 2016 +0900

    [ZEPPELIN-1743] Use explicit arguments in functions in paragraph.controller.js

    ### What is this PR for?
    This is `paragraph.controller.js` maintenance PR. It refactors most functions in paragraph controller to accept explicit parameters instead of getting data from `$scope`. It is a first - simple and safe - step in making paragraph controller more maintainable.

    ### What type of PR is it?
    Refactoring

    ### Todos
    * [x] - Call functions explicitly with parameters when makes sense
    * [x] - Do minor refactoring when makes sense

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1743

    ### How should this be tested?
    Make sure that paragraph works as expected by interacting with it;

    Author: felizbear <ilya@nflabs.com>

    Closes #1714 from felizbear/fe-explicit-arguments and squashes the following commits:

    3c4384f [felizbear] fix test
    d178848 [felizbear] remove unused event listener runParagraph
    4e7db8d [felizbear] make arguments explicit in scroll up / down util functions
    98376e4 [felizbear] remove custom prototype string method startsWith in favor of indexOf
    e65cc18 [felizbear] pass whole paragraph to commitParagraph
    620a6e0 [felizbear] make arguments explicit in various util functions
    a9ddba6 [felizbear] refactor getProgress
    9af71a0 [felizbear] make arguments explicit in autoAdjustEditorHeight
    40ca7d1 [felizbear] make arguments explicit in getEditorSetting
    3315b3c [felizbear] make arguments explicit in aceChanged
    ba9a429 [felizbear] make arguments explicit in toggleOutput
    569368c [felizbear] remove unused function toggleGraphOption
    41772ac [felizbear] make arguments explicit in changeColWidth
    b44972a [felizbear] make arguments explicit in show / hide lineNumbers
    58dcf3a [felizbear] make arguments explicit in show / hide / set title
    83f3b0f [felizbear] make arguments explicit in various open / close editor / table nonsense functions
    62cb81a [felizbear] make arguments explicit in open / close table
    414fd9e [felizbear] make arguments explicit in toggleEditor
    c383a2f [felizbear] make arguments explicit in clearParagraphOutput
    d807e6e [felizbear] make arguments explicit in removeParagraph
    7bbb7dc [felizbear] make arguments explicit in insertNew
    e9e584e [felizbear] make arguments explicit for moveUp and moveDown
    99d23cf [felizbear] make arguments explicit in run
    20d66e5 [felizbear] make arguments explicit in toggleEnableDisable
    c2939d0 [felizbear] make arguments explicit in saveParagraph
    abe56ed [felizbear] make arguments explicit in cancelParagraph
    d25060b [felizbear] make arguments explicit in isRunning
    13d3140 [felizbear] make arguments explicit in initializeDefault