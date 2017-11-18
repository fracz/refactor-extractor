commit daf98e941e140e8739458126640183b9f296a2ab
Author: Chet Haase <chet@google.com>
Date:   Mon Jan 10 14:10:36 2011 -0800

    Use optimized display lists for all hwaccelerated rendering

    Previously, display lists were used only if hardware acceleration
    was enabled for an application (hardwareAccelerated=true) *and* if
    setDrawingCacheEnabled(true) was called. This change makes the framework
    use display lists for all views in an application if hardware acceleration
    is enabled.

    In addition, display list renderering has been optimized so that
    any view's recreation of its own display list (which is necessary whenever
    the visuals of that view change) will not cause any other display list
    in its parent hierarchy to change. Instead, when there are any visual
    changes in the hierarchy, only those views which need to have new
    display list content will recreate their display lists.

    This optimization works by caching display list references in each
    parent display list (so the container of some child will refer to its
    child's display list by a reference to the child's display list). Then when
    a view needs to recreate its display list, it will do so inside the same
    display list object. This will cause the content to get refreshed, but not
    the reference to that content. Then when the view hierarchy is redrawn,
    it will automatically pick up the new content from the old reference.

    This optimization will not necessarily improve performance when applications
    need to update the entire view hierarchy or redraw the entire screen, but it does
    show significant improvements when redrawing only a portion of the screen,
    especially when the regions that are not refreshed are complex and time-
    consuming to redraw.

    Change-Id: I68d21cac6a224a05703070ec85253220cb001eb4