commit a7713069a13b47efbf197a1c7416500c45d14d7c
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Sat Jul 25 08:03:45 2015 -0700

    HttpObjectDecoder performance improvements

    Motivation:
    The HttpObjectDecoder is on the hot code path for the http codec. There are a few hot methods which can be modified to improve performance.

    Modifications:
    - Modify AppendableCharSequence to provide unsafe methods which don't need to re-check bounds for every call.
    - Update HttpObjectDecoder methods to take advantage of new AppendableCharSequence methods.

    Result:
    Peformance boost for decoding http objects.