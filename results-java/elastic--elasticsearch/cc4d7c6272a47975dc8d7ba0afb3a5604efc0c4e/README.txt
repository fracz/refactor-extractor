commit cc4d7c6272a47975dc8d7ba0afb3a5604efc0c4e
Author: mikemccand <mail@mikemccandless.com>
Date:   Wed Jul 23 05:58:31 2014 -0400

    Core: don't load bloom filters by default

    This change just changes the default for index.codec.bloom.load to
    false: with recent performance improvements to ID lookup, such as
    #6298, bloom filters don't give much of a performance gain anymore,
    and they can consume non-trivial RAM when there are many tiny
    documents.

    For now, we still index the bloom filters, so if a given app wants
    them back, it can just update the index.codec.bloom.load to true.

    Closes #6959