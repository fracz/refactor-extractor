commit 456e9ebb9e2dd8bfaff07c3c51cee1753ff72824
Author: Kay Roepke <kay.roepke@xing.com>
Date:   Mon Mar 5 16:52:10 2012 +0100

    start refactoring of Main

    remove pid file on exit
    move all non-essential init stuff to run method so we can make them non-static eventually.