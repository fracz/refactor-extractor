commit 869508f52ce95cceb3f87b28d69f62451dc11f34
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Apr 6 13:54:44 2016 +0200

    js: convert to arrow function intention. for WEB-16918 New refactorings (JS): convert to arrow function/convert to anonymous function
    NB intention is not suggested if this is used in function code (would require adding additional parameter to function)
    + mark es6-refactorings with marker interface