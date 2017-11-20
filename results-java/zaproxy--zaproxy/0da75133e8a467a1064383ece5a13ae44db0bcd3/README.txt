commit 0da75133e8a467a1064383ece5a13ae44db0bcd3
Author: thc202 <thc202@gmail.com>
Date:   Fri Jul 1 22:43:14 2016 +0100

    Improve scroll (speed) in Sites tab

    Change SiteMapPanel to use a panel that implements Scrollable allowing
    to improve the scroll (speed) in the two trees show one above the other
    (contexts and sites, respectively). The panel that was being added
    didn't implement Scrollable leading to slow (the default) speed when
    scrolling. The new panel, ContextsSitesPanel, delegates the scrolling to
    the trees behaving/scrolling as if it was a single tree.
    Change NodeSelectDialog to also use the new class, it also shows the
    contexts and sites tree one above the other in a scroll pane.

    Fix #2642 - Slow mouse wheel scrolling in site tree