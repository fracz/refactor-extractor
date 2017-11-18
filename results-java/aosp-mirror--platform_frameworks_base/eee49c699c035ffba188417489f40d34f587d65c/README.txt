commit eee49c699c035ffba188417489f40d34f587d65c
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Thu Mar 24 17:21:23 2011 -0700

    Fix text redering issue where the text was sometimes truncated

    - mostly was visible in Settings apps / Wi-Fi networks summary info for each network
    - correctly setup the local SkPaint for advances computation
    - improve test app for adding live resizing

    Change-Id: Ia031fe1b115b521ba55c7e68f2a26300f02e48ca