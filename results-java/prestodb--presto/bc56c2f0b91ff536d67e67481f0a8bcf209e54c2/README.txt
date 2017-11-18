commit bc56c2f0b91ff536d67e67481f0a8bcf209e54c2
Author: Christopher Berner <christopherberner@gmail.com>
Date:   Fri May 27 11:14:01 2016 -0700

    Optimize bit packed long decoding in ORC reader

    This makes the bit unpacking ~5x faster in synthetic benchmarks, for
    unpacking large numbers of values and improves overall CPU efficiency
    by 25% for the query I'm running (a scan-filter-aggregate). However, it
    reduces efficiency when unpacking a small number of narrow values, by up
    to ~2x.