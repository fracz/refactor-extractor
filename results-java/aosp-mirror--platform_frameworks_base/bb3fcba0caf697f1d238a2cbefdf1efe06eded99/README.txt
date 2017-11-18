commit bb3fcba0caf697f1d238a2cbefdf1efe06eded99
Author: Jeff Brown <jeffbrown@android.com>
Date:   Mon Jun 6 19:23:05 2011 -0700

    Touch pad improvements.
    Bug: 4124987

    Only show one spot per touch point instead of one spot per
    finger for multitouch gestures.

    Tweaked the pointer acceleration curves.

    Dissociated the hover/tap timeouts from the "tap" timeout
    since they mean very different things.

    Change-Id: I7c2cbd30feeb65ebc12f6c7e33a67dc9a9f59d4c