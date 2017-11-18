commit f5c46c4a99291d2c53d8cbd0fb079c77bf15d8ce
Author: Yun Peng <pcloudy@google.com>
Date:   Wed Aug 10 17:12:31 2016 +0000

    Put runfiles tree under 'runfiles' directory to avoid conflict

    Currently, in python executable zip file, the default workspace
    name "__main__" conflicts with __main__.py file.
    This change fixes it.

    Also refactored the bazel_windows_example_test

    --
    Change-Id: I8b9d64d72335148dba41032ce93643d34670a771
    Reviewed-on: https://bazel-review.googlesource.com/#/c/5351
    MOS_MIGRATED_REVID=129879570