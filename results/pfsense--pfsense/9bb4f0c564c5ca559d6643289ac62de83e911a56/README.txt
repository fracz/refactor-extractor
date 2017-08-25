commit 9bb4f0c564c5ca559d6643289ac62de83e911a56
Author: Bill Marquette <billm@pfsense.org>
Date:   Sun Jul 29 14:24:02 2007 +0000

    Make group manager tree work again
            Correctly toggles items on/off
            Removes checkbox which served no real value
            Split group manager into multiple files like most other screens
            refactored tree generation code

    Known bugs:
            Initial page display incorrectly displays all tree items as allowed
            The changes to the group['pages'] array likely means auth won't work if you don't have "ANY" pages set

    TODO:
            If you have all permissions set, instead of using individual pages, it should set array to ANY
            Allow for entire tree folder selection