commit 010e561a703381e16dd17caa7c942dea6978e6dc
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu May 29 17:48:33 2014 -0700

    Add dumpsys to dock observer.

    Minor refactoring of dock observer to allow its state to be
    inspected and modified via dumpsys for debugging purposes.

    eg. View current state.
    adb shell dumpsys DockObserver

    eg. Simulate being docked.
    adb shell dumpsys DockObserver set state 1

    eg. Reset back to normal.
    adb shell dumpsys DockObserver reset

    Change-Id: Ie48db775290ebed9aa4d9d9d5ac5a6fcb6122ac9