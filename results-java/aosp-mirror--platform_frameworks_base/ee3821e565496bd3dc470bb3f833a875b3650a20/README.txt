commit ee3821e565496bd3dc470bb3f833a875b3650a20
Author: Mady Mellor <madym@google.com>
Date:   Fri Jun 5 11:12:01 2015 -0700

    Insertion cursor: add some slop for moving between lines

    Applying same method to insertion cursor as the text selection drag
    handles to improve moving along lines.

    Basically adds some slop above / below the current line and only
    allows you to change lines if you move outside of that.

    Bug: 21306433
    Change-Id: I6c7f3a496fbd1ea66936832f96325736cea872aa