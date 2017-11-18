commit 76283ae855d7f13e128a34261516a1f82fff60f6
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Sun Jan 22 20:38:01 2017 +0100

    Test dbs gets null log and skips dump startup diagnostics

    this to improve test performance. The biggest benefit of having the null logger
    is that startup diagnostics aren't dumped at all, due to most diagnostics being
    dumped using Logger#bulk, which is now a no-op in null logger.

    It turns out that quite a large amount of time starting a db is spent dumping
    startup diagnostics.

    Overall most tests should benefit from this speedup, e.g. kernel tests run
    roughly 10% faster.