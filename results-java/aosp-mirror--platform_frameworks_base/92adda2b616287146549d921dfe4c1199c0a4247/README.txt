commit 92adda2b616287146549d921dfe4c1199c0a4247
Author: Seigo Nonaka <nona@google.com>
Date:   Tue Jun 2 16:27:12 2015 +0900

    Use final in favor of initialization safety.

    Making a member field final would be beneficial not
    only for the readability but also for the initialization
    safety.
    Leaving SpellCheckerSession#mSpellCheckerSessionListener
    non-final does not make sense not only because we never
    change that member field once SpellCheckerSession object
    is created and but also because SpellCheckerSession
    instance is designed to be accessed from multiple threads
    at the same time, no matter if it has something to do
    with Bug 18945456 or not.

    Change-Id: I1a7ebb54a5d0beddee8799fc5b0800c6e1059099