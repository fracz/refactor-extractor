commit acea257fc2ec1f4b88eb556d3fddf738f1c8c839
Author: glorioso <glorioso@google.com>
Date:   Fri Oct 28 14:15:28 2016 -0700

    New check for subclasses of InputStream

    if a subclasser of InputStream implements int read(), they should also
    override int read(byte[], int, int), otherwise the performance of the
    stream is likely to be slow.

    RELNOTES: New Check: InputStream overrides should also override
    int read(byte[], int, int) to improve the speed of multibyte reads.

    MOE_MIGRATED_REVID=137551912