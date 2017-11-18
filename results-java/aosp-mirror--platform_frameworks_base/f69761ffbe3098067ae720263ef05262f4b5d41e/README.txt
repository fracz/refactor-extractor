commit f69761ffbe3098067ae720263ef05262f4b5d41e
Author: Felipe Leme <felipeal@google.com>
Date:   Thu Feb 23 17:52:01 2017 -0800

    Refactored savableIds() into a SaveInfo class.

    For now it's a "1-to-1" refactoring that keeps the same
    functionalities, but soon SaveInfo will be expanded to
    allow the AutoFillService to customize it.

    Bug: 35727295
    Test: CtsAutoFillServiceTestCases pass
    Test: m update-api

    Change-Id: I5aaa705be2b32590048f70ed0142437e05df94b7