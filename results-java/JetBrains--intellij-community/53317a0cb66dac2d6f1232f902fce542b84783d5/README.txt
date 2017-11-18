commit 53317a0cb66dac2d6f1232f902fce542b84783d5
Author: Anton Tarasov <anton.tarasov@jetbrains.com>
Date:   Wed Sep 20 16:19:01 2017 +0300

    IDEA-171404 allow UI scale exceed 2x

    - UI scale can exceed 2x now
    - JBUI.ScaleType.OBJ_SCALE is introduced
    - JBUI.Scaler & JBUI.ScaleContext is introduced
    - JBUI.JBIcon and subclasses are refactored
    - CachedImageIcon is simplified
    - ShadowPainter scales shadow now
    - Transitioning from int/float to double where applicable
    - double [x, y, width, height] is rounded this way [floor, floor, ceil, ceil]
    - JBDimension is backed by double size
    - JBDimension rescales itself via new methods: size()/width()/width2D()