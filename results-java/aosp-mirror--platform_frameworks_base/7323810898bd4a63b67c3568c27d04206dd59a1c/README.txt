commit 7323810898bd4a63b67c3568c27d04206dd59a1c
Author: Alan Viverette <alanv@google.com>
Date:   Mon Nov 16 16:55:58 2015 -0500

    Clean up PopupMenu

    No functional changes, only refactoring:
    - remove unused mShowCascadingMenus member variable
    - move private interface implementations to anonymous inner classes
    - move interfaces to end of class
    - clean up docs formatting

    Change-Id: Ib82ca0d3a3ff49207959a17b77c4ff4f11a1afc2