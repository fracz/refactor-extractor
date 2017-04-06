commit 924e68c7d522a1086969f3583d0ce87e59110bc5
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Fri Dec 12 16:20:52 2014 -0500

    fix(ngAria): trigger digest on ng-click via keypress, pass $event to expression

    Minor improvement to ng-click directive from ngAria. Now, if bindings are updated
    during the click handler, the DOM will be updated as well. Additionally, the $event
    object is passed in to the expression via locals, as is done for core event directives.

    Closes #10442
    Closes #10443
    Closes #10447