commit bd693a5f85893e9fb42e90c0de90dfeba41ccaec
Author: Chris Lang <clang@fb.com>
Date:   Tue Aug 28 11:13:10 2012 -0700

    [android-sdk] Added PlacePickerFragment, more unit tests, sample

    Summary:
    Added PlacePickerFragment and associated sample and unit tests, as well as more unit test for FriendPickerFragment. Both
    samples now display pickers as a secondary Activity, the results of which are displayed in the main Activity when they are
    dismissed.

    Place and friend pickers can now have parameters passed to them both via XML layout and programmatically.

    Did some refactoring of GraphObjectListFragment to prevent parts of it from poking out into the public API.

    Minor bug fix to GraphObjectWrapper that would prevent, e.g., Double from being cast to double.

    Further work remains on PlacePicker: need to implement AS_NEEDED paging mode to retrieve more results when the user scrolls down.
    Both pickers need support for user-specified fields to be returned.

    Test Plan:
    - Ran unit tests
    - Ran FriendPickerSample
    - Ran PlacePickerSample

    Revert Plan:

    Reviewers: mmarucheck, gregschechte, mingfli

    Reviewed By: mmarucheck

    CC: msdkexp@, platform-diffs@lists, msimpson

    Differential Revision: https://phabricator.fb.com/D560847