commit c0462c0c3bdd7d802dd220e0c830e2caa87989e6
Author: Trustin Lee <t@motd.kr>
Date:   Thu Jun 26 17:00:52 2014 +0900

    Optimize PoolChunk

    - Using short[] for memoryMap did not improve performance. Reverting
      back to the original dual-byte[] structure in favor of simplicity.
    - Optimize allocateRun() which yields small performence improvement
    - Use local variable when member fields are accessed more than once