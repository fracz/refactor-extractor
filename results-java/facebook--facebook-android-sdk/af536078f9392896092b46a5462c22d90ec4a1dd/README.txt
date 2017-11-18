commit af536078f9392896092b46a5462c22d90ec4a1dd
Author: Chris Lang <clang@fb.com>
Date:   Thu Nov 1 15:51:02 2012 -0700

    [android-sdk] Renamed GraphObjectListFragment to PickerFragment.

    Summary:
    As part of the refactoring of PickerFragment/GraphObjectListFragment, the old PickerFragment went away and GraphObjectListFragment
    was made public. The second part of the refactoring is to rename GraphObjectListFragment to PickerFragment, which this
    commit does.

    Test Plan:
    - Built project

    Revert Plan:

    Reviewers: mmarucheck, mingfli, karthiks

    Reviewed By: mmarucheck

    CC: caabernathy, platform-diffs@lists

    Differential Revision: https://phabricator.fb.com/D618156