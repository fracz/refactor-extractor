commit 64c3fc8b027f598d63fd13e4d8e0b91b77d7d012
Author: Raghav Sethi <raghavsethi@fb.com>
Date:   Fri Dec 16 19:10:19 2016 -0800

    Parallelize reading from remote sources

    Create multiple ExchangeOperators for a single HttpPageBufferClient,
    which improves performance when a query is bottlenecked on the single
    thread reading from the buffer.