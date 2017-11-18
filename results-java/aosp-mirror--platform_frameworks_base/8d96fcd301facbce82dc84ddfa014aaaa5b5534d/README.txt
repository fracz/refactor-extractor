commit 8d96fcd301facbce82dc84ddfa014aaaa5b5534d
Author: Tsukasa Hashimoto <tsukasa.x.hashimoto@sonymobile.com>
Date:   Tue Mar 10 18:00:26 2015 +0900

    Add PID info into ANR crash log header

    For improvement of crash log analysis, add a PID info of the target
    process into ANR dropbox crash log header.

    In the ANR dropbox crash log, sometimes the process name (Cmd line)
    in a stacktrace section is not an actual app process name, but is
    "zygote" or "<pre-initialized>" instead. This may be caused when App
    ANR happened just after an app process is started. Hence the
    stacktrace of target app process cannot be found by process
    name at crash log analysis. If PID info is provided, it can be used
    to find the stacktrace section instead of the process name.

    Bug: 28713716
    Change-Id: I28624f2b3c8bc0e8b7545de9525a68bad420d6a0