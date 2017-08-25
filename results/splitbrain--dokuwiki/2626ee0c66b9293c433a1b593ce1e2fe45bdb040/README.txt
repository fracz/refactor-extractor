commit 2626ee0c66b9293c433a1b593ce1e2fe45bdb040
Author: chris <chris@jalakai.co.uk>
Date:   Thu Sep 28 18:51:22 2006 +0200

    more utf8_substr improvements (re FS#891 and yesterday's patch)

    - rework utf8_substr() NOMBSTRING code to always use pcre
    - remove work around for utf8_substr() and large strings from ft_snippet()

    darcs-hash:20060928165122-9b6ab-0eefc216f07f9d7e7d8eb62ce26605c28ee340fa.gz