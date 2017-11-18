commit d834c58252dda7d92890dfd491d0cfe4d2504113
Author: Andrei Stingaceanu <stg@google.com>
Date:   Fri Feb 10 12:55:01 2017 +0000

    TextView/LinkMovementMethod/ClickableSpan - touch up revert

    Reverts the change which adds a gesture detector in
    TextView for detecting clicks on ClickableSpans. A click
    is considered MotionEvent.ACTION_UP. Revert needed because
    the singleTapUp refactoring has a potential to break
    existing apps.

    Bug: 23692690
    Test: attached in the same topic
    Change-Id: Ife47fd0608480130123091bc60a7e9dd1efe8785