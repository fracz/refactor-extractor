commit 81f9434ec70136ac2c7277937c3c837cbd7c80d2
Author: Dmitriy Dumanskiy <doom369@gmail.com>
Date:   Wed Jun 14 21:15:58 2017 +0300

    Added test for multi header, HttpObjectDecoder performance improvement for multi header, removed empty else block.

    Motivation:

    For multi-line headers HttpObjectDecoder uses StringBuilder.append(a).append(b) pattern that could be easily replaced with regular a + b. Also oparations with a and b moved out from concat operation to make it friendly for StringOptimizeConcat optimization and thus - faster.

    Modification:

    StringBuilder.append(a).append(b) reaplced with a + b. Operations with a and b moved out from concat oparation.

    Result:
    Code simpler to read and faster.