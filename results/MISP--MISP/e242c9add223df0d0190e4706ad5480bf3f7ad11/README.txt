commit e242c9add223df0d0190e4706ad5480bf3f7ad11
Author: Iglocska <andras.iklody@gmail.com>
Date:   Tue Oct 6 01:16:48 2015 +0200

    Set of changes to the sync

    - finished preview feature
      - can now view events and attributes remotely
      - can copy over new event to local instance

    - new sync mode (update)
      - allows to only pull changes to events that exist locally already
      - works well with the manual pull of events, no need to pull events that we didn't manually confirm, but can still update all events that we pulled over

    - Fixed an issue with background tasks causing the logging to fail

    - reworked connection test showing version numbers of both instances
      - also telling the admin whether the sync is compatible or not

    - Further refactoring / tweaking of the vent view