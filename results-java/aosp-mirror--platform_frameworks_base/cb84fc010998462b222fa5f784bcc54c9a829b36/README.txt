commit cb84fc010998462b222fa5f784bcc54c9a829b36
Author: Jean-Michel Trivi <jmtrivi@google.com>
Date:   Wed May 3 12:16:17 2017 -0700

    Audio service: only duck started players + refactor

    Refactor management of list of ducked players:
      DuckingManager has a list of DuckedApps, which reference
      the ducked players per uid.
    Only consider ducking a player when it is in STARTED state.
    When a player is released, remove it from the list of ducked players.

    Test: play audio in GPM while having driving directions, music ducks
    Bug: 37433811
    Change-Id: I038a963432c0df6c9470a3a4fb80049d55e8719c