commit 953171db5ee3b0ff8c139a8d4728fbe19e4978e8
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Fri Sep 30 09:17:30 2016 -0700

    Reset freezing flag when window is no longer freezing screen

    Reseting the flag was overlooked during some code refactoring.

    Bug: 31794753
    Test: Manual testing and existing unit tests pass
    Change-Id: Ie35a049cd79f238b3d3d826a00949999f6fb7848