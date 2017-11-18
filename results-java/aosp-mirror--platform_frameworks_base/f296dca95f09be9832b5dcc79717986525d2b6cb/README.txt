commit f296dca95f09be9832b5dcc79717986525d2b6cb
Author: Romain Guy <romainguy@google.com>
Date:   Mon Jun 24 14:33:37 2013 -0700

    (Small) 9patch drawing improvements

    Save a bit of memory in meshs generated from native code
    Avoid an extra if/else when drawing with hardware accelration on

    Change-Id: I31a4550bde4d2c27961710ebcc92b66cd71153cc