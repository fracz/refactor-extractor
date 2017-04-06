commit 7da22e66855b5794d12d0e99e44f0c6f52ce2d55
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Thu Sep 24 21:00:52 2015 +0200

    chore(docs): improve layout of search results

    The API section now uses a multi-column list. This preserves the actual
    order of items. Note that only browser that support @supports and
    columns get the new behavior.
    The line-breaking behavior of search results is also improved. Previously,
    long words would break onto new lines or run into the second column.