commit 2794eb3b02e2404d453d3ad22a8a85a138130a07
Author: Chet Haase <chet@google.com>
Date:   Tue Oct 12 16:29:28 2010 -0700

    Remove generics from Animator APIs

    Change the manner of constructing Animator-related objects from constructors
    via generics to factory methods with type-specific method names. Should
    improve the proliferation of warnings due to generics issues and make the
    code more readable (less irrelevant angle brackets Floating around).

    Change-Id: Ib59a7dd72a95d438022e409ddeac48853082b943