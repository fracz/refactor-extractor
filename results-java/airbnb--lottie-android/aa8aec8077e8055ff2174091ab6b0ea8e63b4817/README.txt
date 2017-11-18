commit aa8aec8077e8055ff2174091ab6b0ea8e63b4817
Author: Gabriel Peal <gabriel.peal@airbnb.com>
Date:   Tue Sep 5 14:05:57 2017 -0700

    Refactoried the way progress is handled in LottieDrawable and LottieValueAnimator

    Previously, progress was held in both the drawable and animator. This made things
    really confusing when min and max progresses were set both because they are in both
    places and because of the difference between animated fraction and animated value.
    This refactors progress to be wholly owned by the animator.
    Fixes #448