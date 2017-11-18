commit cde40a8ab329c1725a7a83eb0fec1783db6efbf9
Author: Chris Lang <clang@fb.com>
Date:   Thu Aug 23 16:03:03 2012 -0700

    [android-sdk] Adds basic Activity/Fragment testing to SdkTests.

    Summary:
    This commit adds basic functionality around testing some of the new UI components (specifically, GraphObjectPagingLoader and
    FriendPickerFragment). Testing is light at the moment, this serves mostly to get the infrastructure in place for further tests.

    FacebookTestCase is split into a base class, FacebookActivityTestCase, which can be used on arbitrary Activity classes;
    FacebookTestCase itself continues to use a trivial Activity that does nothing, and is appropriate for non-UI-centric tests.

    FriendPickerFragmentTests shows how to use the new base class to test an activity containing a fragment. Future commits will
    likely refactor this into a base class that can be used on arbitrary fragments, as we add more fragments.

    Moved the support library out from individaul projects and added it as a project-level lib, to reflect the fact that it will be
    used in multiple libs and samples.

    Test Plan:
    - Ran the unit tests

    Revert Plan:

    Reviewers: mmarucheck, jacl, gregschechte

    Reviewed By: mmarucheck

    CC: msdkexp@, platform-diffs@lists

    Differential Revision: https://phabricator.fb.com/D557204