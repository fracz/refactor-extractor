commit 9ee93076cf04a466d2e9620bc3efe538d93e5983
Author: chris <chris@jalakai.co.uk>
Date:   Thu Aug 31 02:34:13 2006 +0200

    search improvements

    ft_snippet()
    - make utf8 algorithm default
    - add workaround for utf8_substr() limitations, bug #891
    - fix some indexes which missed out on conversion to utf8
      character counts
    - minor improvements

    idx_lookup()
    - minor changes to wildcard matching code to improve performance
      (changes based on profiling results)

    utf8
    - specifically set mb_internal_coding to utf-8 when mb_string
      functions will be used.

    darcs-hash:20060831003413-9b6ab-712021eda3c959ffe79d8d3fe91d2c9a8acf2b58.gz