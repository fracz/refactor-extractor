commit 090e2637385d362c341f01d5c1a833a8b6b23ca2
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Jul 14 23:04:48 2012 +0200

    Finished replacing the old Event Dispatcher with the new one

    With this fix we have removed all traces of the previous EventDispatcher and
    changed the implementation, document and tests to make use of the new
    Dispatcher.

    A few more refactorings are needed to make the code pretty and well
    structured but the basic functionality is there.