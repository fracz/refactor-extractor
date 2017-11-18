commit 33a2b2d0c55ad9201549e09de48b40410af7a567
Author: aquilescanta <aquilescanta@google.com>
Date:   Thu Dec 17 10:15:15 2015 -0800

    Added test cases to the MP4Webvtt parser

    This CL is prepares the ground for refactoring the Webvtt parser,
    so as to use the common parsing algorithms in both parsers. In order
    to do this, the Webvtt Parser will be refactored. As a side note, many
    more test cases will be added once the new subtitle features are
    implemented. Some useful test cases have also been left for a following
    CL, to allow an easy code review.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=110466914