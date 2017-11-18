commit 71afc39d68413d125d46ea69e6c9fb077016bb9b
Author: Gilles Debunne <debunne@google.com>
Date:   Thu May 10 10:24:20 2012 -0700

    Cut long text into multiple DL at start

    Bug 5763685

    To improve performance, preventively cut the the into display
    list of 3-10 lines of text. Further updates to small parts of
    the text (such as adding an underline on a word) will only
    invalidate and redraw the affected sub display list.

    DLs are aligned with paragraphs, just like they will be during
    text edition.

    Change-Id: I0d60debc7fdaea8b29080a6eacb2d60205e7d547