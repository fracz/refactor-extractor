commit 45ae04ab7b02fe8c7a6f2831aeec2e2c8e0cbc6f
Author: Jeremy Klein <jlklein@google.com>
Date:   Fri Feb 5 10:37:32 2016 -0800

    Fix a small issue with the BT Tether callback value.

    It was incorrect when disabling bt tethering. Note that this had
    no practical effect because there's no callback for disabling, but
    the change is good for readability's sake.

    Change-Id: Id80de58b24e94ec5a8ee38d94fb3016ae7835e43