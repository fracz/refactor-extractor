commit d43909c7503e11eb335a452d296a10804bb01fd6
Author: Xavier Ducrohet <xav@android.com>
Date:   Thu Dec 23 07:16:21 2010 -0800

    LayoutLib: add support for unsupported drawing modifiers.

    DrawFilter, Rasterizer, ColorFilter and MaskFilter
    are not supported but we need to provide their
    JNI counterparts anyway, to at least display warnings
    when they are used.

    Also improved the API to query Paint for Shaders
    and PathEffects, and clean up some code by
    moving asserts into the DelegateManager.

    Change-Id: I8942514565d28576d5608c6373bda25d86d42ff2