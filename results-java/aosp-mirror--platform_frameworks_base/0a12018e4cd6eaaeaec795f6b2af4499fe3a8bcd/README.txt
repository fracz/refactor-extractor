commit 0a12018e4cd6eaaeaec795f6b2af4499fe3a8bcd
Author: Ali Utku Selen <aliutku.selen@sonyericsson.com>
Date:   Wed Feb 9 14:11:22 2011 +0100

    Change StringTokenizer to TextUtils.StringSplitter

    Replacing StringTokenizer to TextUtils.StringSplitter,
    since TextUtils.StringSplitter is more suitable for basic splitting tasks.
    Also increased initial values for HashMap and StringBuilders to avoid
    unnecessary buffer enlargement operations. This improves the performance
    of these operations.

    Change-Id: If9a5b68e6596ba9a6d29597876b6164ef34b57ac