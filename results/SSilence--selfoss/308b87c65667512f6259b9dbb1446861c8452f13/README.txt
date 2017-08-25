commit 308b87c65667512f6259b9dbb1446861c8452f13
Author: Jan Tojnar <jtojnar@gmail.com>
Date:   Fri Jun 2 09:26:22 2017 +0200

    Change Teltarif and MMOspy spouts to use FullTextRSS

    MMOspy spout is broken and Teltarif contains advertising system
    artifacts or something, which makes the FullTextRss filters better
    for these sites.

    To improve their quality and lower the maintenance burden, the spouts
    were modified to use FullTextRss (Graby) spout.

    In the future, the spouts can be completely removed and users migrated
    to the actual FullTextRss spout.

    See also: #935