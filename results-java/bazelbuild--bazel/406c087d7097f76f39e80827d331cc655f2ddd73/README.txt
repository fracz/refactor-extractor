commit 406c087d7097f76f39e80827d331cc655f2ddd73
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Thu Jul 28 21:32:01 2016 +0000

    Fix leftover "size" check when deciding whether to use digest or mtime. Since we no longer stored mtime for empty files, this bug meant that we always compared empty files equal (which is good). But we shouldn't be using Metadata based on mtime for them.

    A follow-up change will do a refactor to make this impossible.

    --
    MOS_MIGRATED_REVID=128742054