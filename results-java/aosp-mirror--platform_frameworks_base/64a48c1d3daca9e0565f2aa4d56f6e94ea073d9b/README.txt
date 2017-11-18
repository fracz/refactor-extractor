commit 64a48c1d3daca9e0565f2aa4d56f6e94ea073d9b
Author: Chet Haase <chet@google.com>
Date:   Mon Feb 13 16:33:29 2012 -0800

    Refactor ViewGroup.drawChild() into View.draw()

    Some of the ongoing and upcoming jank work involves having
    Views optimize their rendering. For example, it would be more
    efficient for native display lists to be able to redraw themselves with
    updated transform/alpha properties than it would be to do it the
    way we do now, which causes view hierarchy invalidation and display
    list recreation.

    In order to do this, we need to push more intelligence for view
    rendering into the Views themselves, rather than the complicated
    mechanism we have now of ViewGroup handling some View properties
    (transforms and alpha) and the Views handling the rest of their
    rendering.

    The first step toward this is to take the current drawChild() method
    and push it into a new, package-private method in View that does the
    same thing.

    Future checkins will refactor the code further, simplifying it and
    eventually optimizing around view property changes.

    Change-Id: Id44b94536fc3ff80b474db7ef06862f4f51eedce