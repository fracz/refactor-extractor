commit 94a8acba754e70c463848f7f62414f6ad9e1dcdf
Author: Roozbeh Pournader <roozbeh@google.com>
Date:   Thu Mar 9 16:05:49 2017 -0800

    Check for existance of hyphenation pattern files first

    This CL checks for existance (and readability) of hyphenation pattern
    files before trying to read them. The main impact is reducing the
    spam in the log due to the failure of calling RandomAccessFile() with
    non-existing paths.

    Test: Manual
    Bug: 31727175
    Bug: 36023892
    Change-Id: I6963790fa205ab16d4ece548e4cbb0c15e279a14