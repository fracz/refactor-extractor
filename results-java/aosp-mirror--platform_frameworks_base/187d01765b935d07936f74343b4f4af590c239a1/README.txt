commit 187d01765b935d07936f74343b4f4af590c239a1
Author: Jungshik Jang <jayjang@google.com>
Date:   Tue Jun 17 17:48:42 2014 +0900

    Add SystemAudioAutoInitiationAction and SystemAudioStatusAction

    Once all device discovery action is done if there is audio amplifier
    on device list, it should trigger system audio initiation action.
    On or off of system audio is decided by Tv's last audio setting
    (speaker). If system audio was the last audio setting, it will
    try to turn on system audio; otherwise will turn it off.

    In other hands, SystemAudioStatusAction is added to update
    system audio status (mute or volume) after
    SystemAudioAutoInitiationAction. In fact, RequestArcAction has
    almost same code as it has and will refactore RequesArcAction
    in the following changes.

    Change-Id: I3d591242e79549cb73e14546f0d057ba08f878ef