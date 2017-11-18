commit be565dfc1c17b7ddafa9753851b8f82849fd3f42
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Mon Apr 28 17:51:23 2014 +0200

    Refactored the layout of a notifications

    Notifications now consist of ExpandableViews instead of SizeAdaptiveLayouts
    to avoid layout passes during the resizing. The StackScrollAlgorithm and its
    States are also refactored in order to support the new behaviour. In addition,
    the generation of the outline is moved to the notification views instead of
    the container which contains them.

    Change-Id: I1ac1292a6520f5951610039bfa204c204be9d640