commit fbe94ece70c8064e9d9d6f6c278bb9ab310911cd
Author: Doris Liu <tianliu@google.com>
Date:   Wed Sep 9 14:52:59 2015 -0700

    More refactor on ValueAnimator

    mPlayingBackwards is confusing when placed next to mReversing.
    One means the overall direction of animation playing, the other
    indicates the direction of playing in the current iteration.

    mCurrentIteration and mPlayingBackward are redundant together,
    as the latter can be derived from the former, given the overall
    direction of animation playing. Redundant variables pose risk of
    getting out of sync and therefore are refactored out in this CL.
    Instead, an overall fraction that ranges from 0 to mRepeatCount + 1
    was introduced. It can capture both the current iteration and the
    fraction in the current iteration. It gives a much better idea of
    the overall progress of the animation.

    Change-Id: Ic0ea02c86b04cfc01c462687d1ebbd46184cbab7