commit 42100550f3b7aa092a139977a42893a95c086fcf
Author: Damien CORNEAU <corneadoug@gmail.com>
Date:   Thu Dec 10 18:59:10 2015 +0900

    Improve/split paragraph html

    ### What is this PR for?
    This PR is the first step to divide the paragraph into smaller components.
    In order to work gradually towards making paragraph.js more maintainable, this PR will focus only on splitting the HTML.

    ### What type of PR is it?
    Refactoring

    ### Todos
    * [x] - Split Pragraph HTML
    * [x] - Split Notebook action bar HTML
    * [x] - Fix some z-index problems
    * [x] - Fix failing selenium test

    ### Is there a relevant Jira issue?
    No

    ### How should this be tested?
    * There wasn't any real code changes, just moving code, so you can just use Zeppelin Normally
    * For the z-index fixes, you can check that the paragraph menu, progress bar and graph tooltip are not showing on top of the navbar and actionbar

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Damien CORNEAU <corneadoug@gmail.com>
    Author: Damien Corneau <corneadoug@gmail.com>

    Closes #324 from corneadoug/improve/SplitParagraphHtml and squashes the following commits:

    01a2234 [Damien CORNEAU] Fix waitForParagraph xpath in selenium test
    668d297 [Damien CORNEAU] Try bumping selenium wait time for waitParagraph
    69ac2c6 [Damien CORNEAU] Add Header on new files
    6d62852 [Damien CORNEAU] Fix Empty HTML result when slow page loading
    ebdf0bd [Damien CORNEAU] Fix ActionBar Z-index after rebase broke it
    186ae1e [Damien CORNEAU] Finish spliting paragraph + split notebook actionbar
    be550ed [Damien Corneau] Add license in new files
    125c02d [Damien Corneau] Fix z-index levels
    f7df3ba [Damien Corneau] Fix z-index of for nvtooltip
    74fdb49 [Damien Corneau] Fix z-index of paragraph settings and progressbar
    09578fd [Damien Corneau] Separate progress Bar
    400a7dc [Damien Corneau] Split Chart Selector and Paragraph Controls