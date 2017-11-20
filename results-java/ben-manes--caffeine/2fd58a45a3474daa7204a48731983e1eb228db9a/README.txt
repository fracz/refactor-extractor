commit 2fd58a45a3474daa7204a48731983e1eb228db9a
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Mar 18 14:06:35 2015 -0700

    Use one-shot capacity buffer for improved performance

    Previously the buffer was over allocated with a threshold to record reads while
    a drain was in process. This wastes space and requires an extra volatile read
    that adds overhead. The buffer was lossy to avoid contention by allowing
    producers to overwrite each other and not write strictly in a sequential order.
    This reduced contention at the cost of extra stores and potential for garbage
    to be stuck in the buffer until the next full cycle.

    The one-shot buffer peeks at the next write index. If it is free, it attempts
    to write an element and updates the counter. If the buffer is detectably full,
    then a drain is requested.