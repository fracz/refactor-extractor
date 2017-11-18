commit 9df2ffd4205115a68c4fe0651b2072e3e4573dd2
Author: Doug Zongker <dougz@android.com>
Date:   Sun Feb 14 13:48:49 2010 -0800

    tweak the Base64 implementation

    - move the encodeInternal/decodeInternal methods into the inner
      "state" classes

    - tighten up the inner loop of the encoder and decoder a bit, saving
      about 5% of time in both cases

    - improve javadoc

    - other little fixes

    Change-Id: I72e0ce8502c664a32418cea04636ccdbf4fec17c