commit 4d14d499f82c707b87702b6c026bfdb01d16c393
Author: Ben Manes <ben.manes@gmail.com>
Date:   Mon Jul 27 22:25:17 2015 -0700

    A quick hack of an O(1) LFU/MFU for the simulator

    This is pretty ugly, but exists for completeness. If it was to be used
    in practice a lot of readability improvements are needed to make it a
    bit less nasty.

    A frequency list is used where each node is a chain of cache entries.
    On a miss an entry is created and added to the lowest frequency chain,
    1. On a hit the entry is moved to the next higher frequency chain, N+1.
    When an eviction is needed a victim entry is chosen from the lowest
    (LFU) or highest (MFU) chain. To ensure all operations are O(1) the
    empty frequency chains are removed and new ones are added as needed.
    All of these lists cross-cut a hash table which maps the key to the
    node.