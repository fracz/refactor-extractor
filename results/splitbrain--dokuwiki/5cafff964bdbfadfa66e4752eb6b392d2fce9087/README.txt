commit 5cafff964bdbfadfa66e4752eb6b392d2fce9087
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Fri Jun 16 12:45:39 2006 +0200

    better onload handling

    This patch improves the way the window.oninit JavaScript function is
    called. This function is used to initialiaze all JavaScript funcions
    attached to the DOM so it needs to be executed **after** the full DOM
    was parsed by the browser. Unfortunately currently only Mozilla supports
    a DOMContentLoaded event. In all other browsers we had to wait for
    the window.onload event which will only be called after **all** content
    (including images) was loaded - this caused a visible delay on all
    JavaScript generated content (like the toolbar) in non-Mozilla browsers.

    Dean Edwards now presented a solution [1] which will work for all the bigger
    Browsers and is used in this patch.

    The following browsers now should fire the init event right after parsing
    the DOM:

    All Mozilla based browsers
    Internet Explorer
    Safari
    Opera >

    darcs-hash:20060616104539-7ad00-db70d31fcb21cb812cf4982fe80a7d649e2daa1c.gz