commit c060c482855bb9f51e19dde51911645413d85260
Author: Xavier Léauté <xavier@confluent.io>
Date:   Wed May 31 02:20:15 2017 +0100

    KAFKA-5150; Reduce LZ4 decompression overhead

    - reuse decompression buffers in consumer Fetcher
    - switch lz4 input stream to operate directly on ByteBuffers
    - avoids performance impact of catching exceptions when reaching the end of legacy record batches
    - more tests with both compressible / incompressible data, multiple
      blocks, and various other combinations to increase code coverage
    - fixes bug that would cause exception instead of invalid block size
      for invalid incompressible blocks
    - fixes bug if incompressible flag is set on end frame block size

    Overall this improves LZ4 decompression performance by up to 40x for small batches.
    Most improvements are seen for batches of size 1 with messages on the order of ~100B.
    We see at least 2x improvements for for batch sizes of < 10 messages, containing messages < 10kB

    This patch also yields 2-4x improvements on v1 small single message batches for other compression types.

    Full benchmark results can be found here
    https://gist.github.com/xvrl/05132e0643513df4adf842288be86efd

    Author: Xavier Léauté <xavier@confluent.io>
    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jason Gustafson <jason@confluent.io>, Ismael Juma <ismael@juma.me.uk>

    Closes #2967 from xvrl/kafka-5150