commit 0c7a42a09d85ddffd9b860bcb31e4c43a00632c1
Author: Nathan Harmata <nharmata@google.com>
Date:   Thu Oct 13 18:17:48 2016 +0000

    Slight refactor of ExternalFilesHelper:
    -Make FileType and ExternalFileAction public.
    -Have producers use ExternalFileAction, rather than a boolean, to specify the desired behavior.

    And a big change in semantics (doesn't affect Bazel):
    -Replace ExternalFileAction.ERROR_OUT with ExternalFileAction.ASSUME_NON_EXISTENT_AND_IMMUTABLE, which does what it sounds like. This new action, like the old ERROR_OUT, is _not_ used in Bazel.

    --
    MOS_MIGRATED_REVID=136063159