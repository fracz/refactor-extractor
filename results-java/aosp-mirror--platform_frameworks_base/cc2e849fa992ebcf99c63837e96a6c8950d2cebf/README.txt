commit cc2e849fa992ebcf99c63837e96a6c8950d2cebf
Author: Daniel Sandler <dsandler@android.com>
Date:   Tue Nov 13 20:49:47 2012 -0800

    Notification vibration improvements: [DO NOT MERGE]

     - When notifications vibrate as a fallback (that is,
       because they want to play a sound but the device is in
       vibrate mode), this no longer requires the VIBRATE
       permission.
     - As a bonus, if your notifications use DEFAULT_VIBRATE,
       you don't need the VIBRATE permission either.
     - If you specify a custom vibration pattern, you'll still
       need the VIBRATE permission for that.
     - Notifications vibrating in fallback mode use same
       vibration pattern but can be changed easily in future.
     - The DEFAULT_VIBRATE and fallback vibrate patterns are now
       specified in config.xml.

    Bug: 7531442
    Change-Id: I7a2d8413d1becc53b9d31f0d1abbc2acc3f650c6