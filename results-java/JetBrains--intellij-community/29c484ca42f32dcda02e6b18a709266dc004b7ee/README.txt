commit 29c484ca42f32dcda02e6b18a709266dc004b7ee
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Thu May 18 01:25:10 2017 +0300

    PY-18154: Add sudo-like tool for windows to deal with UAC

    When admin user launches Intellij, Windows revokes many
    user privileges to improve safety for admin users (that is how UAC works).

    One can't access "Program Files": any attempt to write something there
    leads on ACCESS_DENIED(5) error even if NTFS reports file is writable

    The only way to elevate privileges is to launch process as elevated.
    "Elevator.sln" is Win32API app that launches command
    as elevated. See its sources for more info.