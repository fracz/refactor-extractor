commit 497a92cc5ba2176b8a8484b0a7da040eac0e887b
Author: Jeff Brown <jeffbrown@google.com>
Date:   Sun Sep 12 17:55:08 2010 -0700

    Add keycodes and meta-key modifiers to support external keyboards.

    Added new key maps for external keyboards.  These maps are intended to
    be shared across devices by inheriting the "keyboards.mk" product
    makefile as part of the device's product definition.

    One of the trickier changes here was to unwind some code in
    MetaKeyKeyListener that assumed that only the low 8 bits of the meta key
    state were actually used.  The new code abandons bitshifts in favor
    of simple conditionals that are probably easier to read anyways.
    The special meta key state constants used by MetaKeyKeyListener
    are now (@hide) defined in KeyEvent now so as to make it clearer that they
    share the same code space even if those codes are not valid for KeyEvents.

    The EventHub now takes care of detecting the appropriate key layout
    map and key character map when the device is added and sets system
    properties accordingly.  This avoids having duplicate code in
    KeyCharacterMap to probe for the appropriate key character map
    although the current probing mechanism has been preserved for legacy
    reasons just in case.

    Added support for tracking caps lock, num lock and scroll lock and
    turning their corresponding LEDs on and off as needed.

    The key character map format will need to be updated to correctly support
    PC style external keyboard semantics related to modifier keys.
    That will come in a later change so caps lock doesn't actually do
    anything right now except turn the shiny LEDs on and off...

    Added a list of symbolic key names to KeyEvent and improved the toString()
    output for debug diagnosis.  Having this list in a central place in the
    framework also allows us to remove it from Monkey so there is one less
    thing to maintain when we add new keycodes.

    Bug: 2912307
    Change-Id: If8c25e8d50a7c29bbf5d663c94284f5f86de5da4