commit 5ac6ecab482d65d87b9594511426b9ccd18709e8
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Mar 3 02:40:45 2017 +0000

    Taxonomy: Improve precision of duplicate name checks when inserting terms.

    `wp_insert_term()` does not allow for terms with the same name to exist
    at the same hierarchy level, unless the second term has a unique slug.
    When this logic was refactored in [31792] and [34809], a bug was
    introduced whereby it was possible to bypass the no-same-named-sibling
    check in cases where the first term had a non-auto-generated slug
    (ie, where the name was 'Foo' but the slug something other than 'foo',
    such that the second term would get the non-matching slug 'foo').

    This changeset fixes this issue by ensuring that the duplicate name
    check runs both in cases where there's an actual slug clash *and* in
    cases where no explicit `slug` has been provided to `wp_insert_term()`.
    The result is a more reliable error condition:
    `wp_insert_term( 'Foo' ... )` will always fail if there's a sibling
    'Foo', regardless of the sibling's slug.

    Props mikejolley.
    See #39984.
    Built from https://develop.svn.wordpress.org/trunk@40144


    git-svn-id: http://core.svn.wordpress.org/trunk@40083 1a063a9b-81f0-0310-95a4-ce76da25c4cd