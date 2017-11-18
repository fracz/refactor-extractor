commit 42471dd5552a346dd82a58a663159875ccc4fb79
Author: Dan Egnor <egnor@google.com>
Date:   Thu Jan 7 17:25:22 2010 -0800

    Simplify & update ANR logging; report ANR data into the dropbox.
    Eliminate the per-process 200ms timeout during ANR thread-dumping.
    Dump all the threads at once, then wait for the file to stabilize.
    Seems to work great and is much, much, much faster.

    Don't dump stack traces to traces.txt on app crashes (it isn't very
    useful and mostly just clutters up the file).

    Tweak the formatting of the dropbox dumpsys a bit, for readability,
    and avoid running out of memory when dumping large log files.

    Report build & kernel version with kernel log dropbox entries.