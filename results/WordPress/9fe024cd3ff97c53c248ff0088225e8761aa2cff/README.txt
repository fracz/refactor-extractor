commit 9fe024cd3ff97c53c248ff0088225e8761aa2cff
Author: Weston Ruter <weston@xwp.co>
Date:   Fri Sep 23 22:23:30 2016 +0000

    Customize: Re-architect and harden panel/section UI logic.

    Removes contents for sections and panels from being logically nested (in the DOM) in order to eliminate many issues related to using `margin-top` hacks. The element containing the link to expand the content element for panels and sections is now a sibling element to its content element: the content is removed from being nested at initialization. The content element is now available in a `contentContainer` property whereas the head element (containing the link to open the construct) is in a `headContainer` property. The existing `container` property is now a jQuery collection that contains both of these elements. Since the head element is no longer in an ancestor element to the `content` element, the `aria-owns` property is now used to maintain the relationship between the `headContainer` and the `contentContainer`. These changes are also accompanied by an improvement to the animation performance for the sliding panes.

    Props delawski, celloexpressions.
    Fixes #34391.
    Fixes #34344.
    Fixes #35947.

    Built from https://develop.svn.wordpress.org/trunk@38648


    git-svn-id: http://core.svn.wordpress.org/trunk@38591 1a063a9b-81f0-0310-95a4-ce76da25c4cd