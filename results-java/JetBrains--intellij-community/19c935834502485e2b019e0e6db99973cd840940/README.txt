commit 19c935834502485e2b019e0e6db99973cd840940
Author: Anton Tarasov <anton.tarasov@jetbrains.com>
Date:   Mon Dec 7 16:55:18 2015 +0300

    IDEA-148739 Enable gutter icons scaling by default

    1. We should use a delegate image of JBHiDPIScaledImage when converting it to BufferedImage. In particular, this worked incorrectly in IconLoader.scale(float) when it converted JBHiDPIScaledImage to BufferedImage and passed the result into Scalr.resize(..).

    2. I refactored JBHiDPIScaledImage class so that it creates a 1x1 dummy wrapper for a delegate image, saving us memory resources.

    3. Switched on "editor.scale.gutter.icons" by default.