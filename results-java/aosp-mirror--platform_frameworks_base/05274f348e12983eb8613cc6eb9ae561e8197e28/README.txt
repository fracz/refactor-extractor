commit 05274f348e12983eb8613cc6eb9ae561e8197e28
Author: Eric Laurent <elaurent@google.com>
Date:   Thu Nov 29 12:48:18 2012 -0800

    AudioService: improve initial safe volume delay

    AudioService relies on a valid mmc in order to enforce the headset
    volume limitation or not. There is a timeout to enforce the limitation
    if no mcc is configured after boot.
    Until this timeout is reached or a valid SIM is detected the headset
    volume is not limited.

    This change makes that the last known volume limitation state (enforced or
    not) is persisted so that next time we boot, last known state is applied until
    a new mcc is configured if any. In most cases, the mcc does not change from one
    boot to the next and we do the right thing. If teh mcc does change, the correct
    policy will be enforced when the mcc is detected or after the timeout.

    Also fix a bug where the volume panel was not displayed if the limitation mechanism
    is triggered at the first press on VOL+ key.

    Bug 7455275.

    Change-Id: Id0f2996d893d38c6a14f4f9e4a0e9e3be17ef127