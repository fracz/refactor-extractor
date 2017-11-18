commit f86bc976261d1f9860a4249ce9ef4d0ef4c8c3ef
Author: Andrei Stingaceanu <stg@google.com>
Date:   Tue Apr 12 15:29:25 2016 +0100

    Keyboard shortcuts: one instance refactor

    Make all the instance public methods private
    and add static methods to control the behavior:
    * show
    * dismiss
    * toggle

    Make sure we only deal with one instance of the
    KeyboardShortcuts (previously both the
    KeyboardShortcutsReceiver and BaseStatusBar were
    creating instances).

    Makes sure the instance is destroyed when dismissing
    and when showing it either creates a new one if none
    exists or reuses the existing one.

    Also fixes an existing nasty issue where in order
    to dismiss the dialog from BaseStatusBar we were
    always first creating a new instance.

    Bug: 28012198
    Change-Id: I207553dd45ae535edc64b6292a472fa0899029b0