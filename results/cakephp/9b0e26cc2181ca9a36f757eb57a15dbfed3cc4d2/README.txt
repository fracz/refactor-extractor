commit 9b0e26cc2181ca9a36f757eb57a15dbfed3cc4d2
Author: mark_story <mark@mark-story.com>
Date:   Sat Aug 31 13:20:41 2013 -0400

    Deprecate MemcacheEngine and update defaults for Memcached

    People should switch to Memcached instead. The underlying extension is
    better maintained and provides improved features and performance.

    Collapse the persistent and persistentId settings, while also making
    non-persistent connections the default. Persistent connections should be
    an opt-in feature as having them enabled by default could go very wrong
    on shared hosting environments.