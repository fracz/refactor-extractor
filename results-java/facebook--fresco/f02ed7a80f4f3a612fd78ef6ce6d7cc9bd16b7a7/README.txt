commit f02ed7a80f4f3a612fd78ef6ce6d7cc9bd16b7a7
Author: Ognjen Dragoljevic <plamenko@fb.com>
Date:   Thu Jun 2 08:59:15 2016 -0700

    Always reserve space for a background and an overlay

    Summary:
    This achieves two things:
    * It allows us to dynamically change backgrounds and overlays
    * It saves memory as we removed 5 instance integers and added only 2 instance null-references (two null elements in the array of the fade drawable)

    An additional change is that multiple backgrounds are from now on deprecated. Context:
    This improvement required a small interface change between GenericDraweeHierarchy and its builder. In particular, GenericDraweeHierarchy now has only one layer dedicated for a background. Multiple backgrounds are still supported via builder, but this gets emulated by wrapping them with an ArrayDrawable. In theory this should work fine, with one caveat. Rounding no longer works for multiple backgrounds. This would be relatively easy to support, but given that multiple backgrounds are rarely used feature (if ever), instead of introducing additional complexity we decided to deprecate it instead.
    In case where there is only one or no background, everything should work as it was before.
    Closes https://github.com/facebook/fresco/pull/1246

    Reviewed By: kirwan

    Differential Revision: D3371737

    Pulled By: plamenko

    fbshipit-source-id: 73b3843631090564f445aab8117f2754e5bc2cb1