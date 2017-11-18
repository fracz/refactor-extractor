commit f10daf647004e84235d240bb6471aa0dab61a493
Author: Ben Murdoch <benm@google.com>
Date:   Wed Nov 16 18:12:57 2011 +0000

    Fix a bug in the database upgrade process, and refactor slightly.

    During upgrade from v9 -> v10 the database version was not
    being updated.

    Also remove conditionals based on what are effectively hardcoded
    truth values to simplfiy the logic greatly.

    Bug: 5560410
    Change-Id: I31a01aa35a109a951d4e4c6d5b884bb666668b28