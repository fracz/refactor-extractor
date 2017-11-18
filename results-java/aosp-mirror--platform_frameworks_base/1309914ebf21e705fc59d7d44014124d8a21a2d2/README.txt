commit 1309914ebf21e705fc59d7d44014124d8a21a2d2
Author: Doris Liu <tianliu@google.com>
Date:   Fri Jul 10 17:32:41 2015 -0700

    Refactor AnimatorSet in prep for adding more functionalities

    This refactor changes how relationships (i.e. with/after/before)
    among Animators in an AnimatorSet are represented. Specifically,
    a directed graph is used: parent-child indicates sequential
    relationship, and siblings will fire simultaneously.

    This CL also fixed the issue where start delays for Animators that
    play together are not handled correctly.

    Bug: 11649073
    Bug: 18069038
    Change-Id: I5dad4889fbe81440e66bf98601e8a247d3aedb2e