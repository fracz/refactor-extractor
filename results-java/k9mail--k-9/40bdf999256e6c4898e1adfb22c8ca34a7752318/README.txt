commit 40bdf999256e6c4898e1adfb22c8ca34a7752318
Author: Dan Applebaum <danapple@danapple.com>
Date:   Fri Feb 4 12:26:14 2011 +0000

    Fixes Issue 2934 Fixes Issue 2935

    Provides for storing Folder Settings in the central Preferences
    Storage as a back-up to the settings stored on each folder.  In this
    way, even if the LocalStore DB is recreated or otherwise lost, Folder
    Settings can be recovered.

    1) Does not change the methodology used to read settings while
    running, nor the changes in r3107 & r3116 which tremendously improve
    Accounts list loading time.

    2) Loads Folder Settings from Preferences and stores on the folder
    only when creating a new LocalFolder

    3) Saves Folder Settings to Preferences and the DB row every time the
    Folder Settings are changed.

    4) When upgrading from DB version 41 to 42 or later, copies all
    settings for existing folders from the DB storage to the Preferences
    Storage.

    5) Transactional bulk folder creation and single pass local folder
    existence check during "Refresh folders" operation drastically reduces
    time spent when refreshing folders from the remote store.

    6) Uses prepared statement during Editor commit to reduce Preference
    storing time.

    Probably needs a reversion of r3239, but I'm unfamiliar with
    translations, so am leaving that to others' discretion.