commit 19e3cbdb35406e0a8d2a401fde84674b024b22cb
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu Apr 6 14:36:39 2017 +0900

    NetworkMonitor: improve captive portal validation logs

    This patch adds probe type and url to all validation logs about web
    probes sent for captive portal detection.

    Test: built, flashed, looked at $ adb shell dumpsys connectivity
    Bug: 36830206, 36532213
    Merged-In: Iee2caaf6664f3d097f9d1888ccc5fece0a95994c

    (cherry picked from commit d9ac87ed65aa12b93e0b70f5e8560562c850fde0)

    Change-Id: I5ab8bb117d2bbad123136dfbbf78cbe84a7f688e