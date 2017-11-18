commit 3660789f06c5fbcb81e6c7c79612048bff8f0f66
Author: Raph Levien <raph@google.com>
Date:   Wed Jun 25 16:32:13 2014 -0700

    Switch all text layout to Minikin

    This patch switches all text layout operations to use Minikin, removes
    the USE_MINIKIN #ifdef, and deletes some of the code that was only used
    in the old TextLayout path (although some more refactoring remains).

    Change-Id: I51b5c4e2bb46cfd9d204c12b9f16f135c769f5b5