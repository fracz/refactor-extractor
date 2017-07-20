commit 11eeb33644c63f1049ed56c7608abe84144f5fa7
Author: Marc Würth <ravage@bluewin.ch>
Date:   Sat Jul 6 19:04:06 2013 +0200

    Some minor CS improvements to FormHelper

    Replaced use of deprecated method getVar by get.
    Improved DocBocks, mostly data types
    Removed deprecated DocBock tag because it's an invalid use of such. This makes the method look like it was deprecated but in reality it's only one of the possible values of one of its parameters.