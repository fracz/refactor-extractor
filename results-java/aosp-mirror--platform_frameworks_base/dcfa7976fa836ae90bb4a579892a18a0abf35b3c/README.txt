commit dcfa7976fa836ae90bb4a579892a18a0abf35b3c
Author: Winson Chung <winsonc@google.com>
Date:   Tue Jul 22 12:27:13 2014 -0700

    Intermediate refactoring to move towards in-app view transitions.

    - Fixing bug where we weren't toggling to the right task when using affiliations
    - Refactoring task rect calculation to allow full screen task view to be laid out for transitions
    - Refactoring the view bounds animations into a separate class
    - Refactoring the footer view (for lock-to-task) out of TaskView
    - Refactoring some transform code out of TaskView
    - Removing fullscreen overlay view
    - Fixing case where extra invalidations and layouts were still happening in FixedSizeImageView
    - Adding debug overlay to replace specific debug drawing code

    Change-Id: Ibf98b6a0782a68cd84582203c807cece1ff3379f