commit 212c7f9465877011f9cbe2e3fc495422282c4667
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Mon Mar 20 00:30:06 2017 +0100

    PSR-4: Removed hardcoded DATA_DIR, improved backup

    Preparations for PSR-4 compliance.

    - Ugly, empty "src/data" folder removed.

    - DATA_DIR constant removed.

    - backup() function enhanced to save to "backups/" and auto-create
      folder if necessary. Now also prints status to terminal for each file.
      Still a very basic and neglected function, but hey it's better now.

    - Revised SettingsFile default storage to use "sessions/" instead of the
      previous, hidden "data" folder. The new path is autocreated if used.
      This means that users are more aware of the fact that sessions exist.