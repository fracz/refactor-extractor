commit 6626a38730050c83a0dd6cdc1bfc510024e9ca95
Author: Phillip Webb <pwebb@vmware.com>
Date:   Mon Dec 31 13:08:39 2012 -0800

    Fix [deprecation] compiler warnings

    Fix deprecation compiler warnings by refactoring code or applying
    @SuppressWarnings("deprecation") annotations. JUnit tests of
    internally deprecated classes are now themselves marked as
    @Deprecated.

    Numerous EasyMock deprecation warnings will remain until the
    migration to mockito can be completed.