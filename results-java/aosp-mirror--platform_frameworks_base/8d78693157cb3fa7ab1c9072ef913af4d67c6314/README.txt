commit 8d78693157cb3fa7ab1c9072ef913af4d67c6314
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed Oct 26 19:08:36 2016 -0700

    The big keyguard transition refactor (6/n)

    Cleanup:
    - Make sure all the state is nicely dumped.
    - Remove some unused stuff.
    - Fix a flicker when occluded -> unlocked

    Bug: 32057734
    Change-Id: Id87e26adccef740d608b325c2dc1f6db14dd4ec3