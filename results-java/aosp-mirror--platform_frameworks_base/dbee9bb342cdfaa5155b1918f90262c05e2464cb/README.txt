commit dbee9bb342cdfaa5155b1918f90262c05e2464cb
Author: Teng-Hui Zhu <ztenghui@google.com>
Date:   Tue Dec 15 11:01:27 2015 -0800

    Gradient for VectorDrawable's fill and stroke

    Add ComplexColor interface for both GradientColor and ColorStateList.
    Set up constant state, factory, theme attrs for GradientColor, while
    refactoring the ColorStateList's similar code. (Functionality in CSL should
    be the same).

    Support themeing in both the root and item level in GradientColor.
    For example, both startColor in <gradient> tag or color in <item> tag can
    have theme color.
    Add tests for both simple and complex cases with themeing etc.

    Hook up the native VectorDrawable implementation using 2 extra JNI calls for
    simplicity. Such calls only happen at inflate and applyTheme call.

    b/22564318

    Change-Id: Ibdc564ddb4a7ee0133c6141c4784782f0c93ce0e