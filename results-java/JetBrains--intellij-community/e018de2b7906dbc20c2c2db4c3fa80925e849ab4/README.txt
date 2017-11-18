commit e018de2b7906dbc20c2c2db4c3fa80925e849ab4
Author: Anton Tarasov <anton.tarasov@jetbrains.com>
Date:   Tue Oct 25 17:01:54 2016 +0300

    Add/refactor JBUI.JBIcon/ScalableJBIcon/ValidatingScalableJBIcon

    - Refactored: EmptyIcon, LayeredIcon, RowIcon, CachedImageIcon, LazyIcon
    - JBUI.scale missed LayeredIcon's