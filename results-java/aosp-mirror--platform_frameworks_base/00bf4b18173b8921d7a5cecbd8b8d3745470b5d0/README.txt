commit 00bf4b18173b8921d7a5cecbd8b8d3745470b5d0
Author: Jean-Michel Trivi <jmtrivi@google.com>
Date:   Fri Jul 26 17:19:32 2013 -0700

    More refactor of audio focus, keep track of focus loss

    Move more audio focus-specific functionality into the class
      representing each audio focus owner, FocusStackEntry.
    Keep track of how each FocusStackEntry instance lost focus.

    Change-Id: I35df0717765a26ec747cb0110e2e951d155d1525