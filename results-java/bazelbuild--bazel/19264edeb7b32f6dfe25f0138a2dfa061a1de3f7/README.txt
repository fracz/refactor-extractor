commit 19264edeb7b32f6dfe25f0138a2dfa061a1de3f7
Author: Ulf Adams <ulfjack@google.com>
Date:   Tue Feb 23 09:34:14 2016 +0000

    Make some JavaCommon static to reduce reliance on mutable state.

    Also change the JavaSemantics interface to provide a top-level method to
    determine the main class for a java_binary rule.

    Also refactor the JavaSemantics implementations to reduce passing of
    JavaCommon.

    --
    MOS_MIGRATED_REVID=115316536