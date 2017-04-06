commit 629ac8e014e5272aae80d2540f47f2d1ae44baa4
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Sep 25 11:44:05 2015 +0200

    Java api: remove TermsLookupQueryBuilder

    TermsLookupQueryBuilder was left around only for bw comp reasons, but TermsQueryBuilder is its replacement. We can remove it now that it is clear query refactoring goes in master (3.0).