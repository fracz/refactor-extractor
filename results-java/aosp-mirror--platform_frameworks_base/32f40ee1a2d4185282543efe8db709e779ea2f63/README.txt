commit 32f40ee1a2d4185282543efe8db709e779ea2f63
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Oct 20 15:54:14 2016 -0700

    Implement Binder shell command for settings provider.

    The provider now published a system service, through which you
    can do direct shell commands.  The commands are a copy of what
    the existing "settings" command does (in a follow-up, that will
    be converted to a simple script that calls this).

    Also improved a few things in the provider:

    - Don't allow implicit creation of settings data for users that
    don't exist.
    - Improve dump to include the package name that applied each setting
    and remove misleading stuff from historical data (and this is now
    available through "dumpsys settings" instead of the provider dump).

    Test: manual

    Change-Id: Id9aaeddc76cca4629d04cbdcbba6a311e942dfa6