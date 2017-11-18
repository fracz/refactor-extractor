commit 1746300aadfd031fc03dafa238338db45743878a
Author: Lari Hotari <lari.hotari@gradle.com>
Date:   Sat Oct 1 11:22:34 2016 +0300

    Fix exec output with multi-byte unicode characters at buffer boundary

    - fixes GRADLE-3329
    - also improves performance and memory usage efficiency of exec output
      buffering by using StreamByteBuffer as the buffer implementation.
    - Flushes lines ending with unix line separator on Windows
      - the output might contain only unix line separators (\n) and that
        would pile up in the buffer on Windows. This is the case with
        running node/npm commands.

    +review REVIEW-6266