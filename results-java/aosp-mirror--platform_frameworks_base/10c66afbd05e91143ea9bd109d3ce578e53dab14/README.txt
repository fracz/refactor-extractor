commit 10c66afbd05e91143ea9bd109d3ce578e53dab14
Author: Jim Miller <jaggies@google.com>
Date:   Thu May 10 15:59:03 2012 -0700

    Fix 6398209: Improve MultiWaveView animations and interaction

    TargetDrawables now have a "home" position they can be scaled about.
    Added new "focused" state to TargetDrawable.  This is used to distinguish between highlight and selection.
    Updated target icons to use new focused state. Currently re-uses "activated" icon.
    Change to event handling to allow cancel events when a target is highlighted to cause a selection.
    Cleaned up MultiWaveView initialization code.

    MultiWaveView animation improvements:
     - new scale animation when center handle is touched.
     - switched to using indices instead of foreach loops in critical path code to avoid creating temporary objects.
     - updated and simplified animation code.

    Change-Id: I593c021475f1644c73bdb9f84855e6a9fec7c0ab