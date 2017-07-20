commit eb8f89bb132cf1b10eba5a3e1f05514d4ac2820e
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Thu Nov 13 20:05:24 2014 +0000

    Don't split shared terms on term update.

    Splitting shared terms means assigning a new term_id to a given term_taxonomy_id.
    It was uncovered that this change could cause problems for sites that have
    cached the original term_id somehow - say, in postmeta - since future lookups
    using that term_id will now fail.

    Removing for 4.1-beta1. We'll look at improvements to backward compatibility
    to try to get this back into a later beta.

    Props mboynes.
    See #30335.
    Built from https://develop.svn.wordpress.org/trunk@30336


    git-svn-id: http://core.svn.wordpress.org/trunk@30335 1a063a9b-81f0-0310-95a4-ce76da25c4cd