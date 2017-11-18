commit c0e7a90f1f5f98e85dbeda021fac0dff79725933
Author: Stan Iliev <stani@google.com>
Date:   Thu Oct 13 17:07:09 2016 -0400

    Initial refactoring to enable reuse of SkiaDisplayList
    on a per RenderNode basis. With Skia renderer we
    see 30% speed improvement in Invalidate Tree UI Jank test,
    when SkiaDisplayList objects are reused.

    Test: manually built and run on angler-eng.
    Change-Id: Ie4ec50ddb2015150e3ec678dde7ebed0c8d90067