commit 199acdfcc98e852975dd7edfbcb822ba5e73146f
Author: Chet Haase <chet@google.com>
Date:   Wed Jul 24 18:40:55 2013 -0700

    Better Transition interruption

    Previously, a running transition on a scene root would simply
    be canceled when a new transition was started. This would result in
    abrupt scene changes, especially in generic use cases where apps/widgets
    would spawn multiple transitions in successive rendering frames due to
    small changes in view properties.

    The new approach is to check all running animations against new transitions.
    If there are overlapping properties that are being set to different values,
    the new animations win and the old ones are canceled. If the end values are the
    same, the new animations are noop'd and the old ones are allowed to continue
    as-is.

    There was also improvement to capturing state while other transitions are
    running, necessary in this new world where old transitions are allowed to
    continue running. Now, transitions are pause()'d while values are captured,
    then resume()'d after capturing is done. This allows the system to see what the
    real view properties are, instead of the mid-animation values.

    Change-Id: I8e77fb9c1967087a682bb26a45763005f5ca9179