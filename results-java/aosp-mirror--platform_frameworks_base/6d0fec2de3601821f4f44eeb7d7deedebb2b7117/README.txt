commit 6d0fec2de3601821f4f44eeb7d7deedebb2b7117
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Jul 23 21:28:06 2010 -0700

    Refactor input reader to support new device types more easily.

    Refactored the input reader so that each raw input protocol is handled
    by a separate subclass of the new InputMapper type.  This way, behaviors
    pertaining to keyboard, trackballs, touchscreens, switches and other
    devices are clearly distinguished for improved maintainability.

    Added partial support for describing capabilities of input devices
    (incomplete and untested for now, will be fleshed out in later commits).

    Simplified EventHub interface somewhat since InputReader is taking over
    more of the work.

    Cleaned up some of the interactions between InputManager and
    WindowManagerService related to reading input state.

    Fixed swiping finger from screen edge into display area.

    Added logging of device information to 'dumpsys window'.

    Change-Id: I17faffc33e3aec3a0f33f0b37e81a70609378612