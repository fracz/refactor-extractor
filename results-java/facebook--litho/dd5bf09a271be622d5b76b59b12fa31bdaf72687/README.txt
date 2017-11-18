commit dd5bf09a271be622d5b76b59b12fa31bdaf72687
Author: Emil Sjolander <emilsj@fb.com>
Date:   Fri May 12 03:50:32 2017 -0700

    Big refactor moving most logic into DebugComponent

    Summary: This adds very little (no?) new features and mostly just refactors code to live in a singular place. Instead of users having to worry about DebugComponent as well as the DebugInfo object and attaching it correctly to a tree now user's of the DebugComponent API only need to worry about a single class, greatly simplifying its usage.

    Reviewed By: passy

    Differential Revision: D5027780

    fbshipit-source-id: 95a95b3572747aa2088f8f9b35a160257eb59269