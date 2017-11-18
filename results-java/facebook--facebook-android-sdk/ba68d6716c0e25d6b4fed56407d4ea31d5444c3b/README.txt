commit ba68d6716c0e25d6b4fed56407d4ea31d5444c3b
Author: Chris Lang <clang@fb.com>
Date:   Mon Jul 23 16:30:59 2012 -0700

    [android-sdk] When putting wrapped GraphObjects, store their underlying JSON representation.

    Summary:
    To improve serialization support, when a GraphObject or GraphObjectList is being stored in another
    GraphObject or GraphObjectList, we really want to store the underlying JSONObject or JSONArray.

    Test Plan:
    - Ran unit tests

    Revert Plan:

    Reviewers: mmarucheck, jacl, gregschechte, ayden

    Reviewed By: mmarucheck

    CC: msdkexp@, platform-diffs@lists, security-diffs@lists

    Differential Revision: https://phabricator.fb.com/D528051