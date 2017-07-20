commit 70c0ca3e4dfa50014857eb153d7c005a5d1502c2
Author: Mark Story <mark@mark-story.com>
Date:   Tue Jul 19 22:50:02 2016 -0400

    Revert "Minor optimization for DateTimeType"

    This reverts commit 2d153f080c16483c8a44346b5ddfce1673263c5e. While
    this minor optimization should have helped improve performance, but has
    created subtle issues in at least one application.

    Refs #9066