commit 6b2a6177aad44466794a9262a4f2f2c209a3f2e5
Author: Yu.Ishihara <Yu.Ishihara@jp.sony.com>
Date:   Wed Mar 25 09:44:06 2015 +0900

    Enable selecting device whose power status is not on

    DeviceSelect() returns immediately if the active routing path
    is selected even if the device is turned off and not displaying
    anything.

    This fix allows device to be selected again if target device's
    power status is not on in order to wake it up, hence improve
    the user experience.

    Bug: 19724238
    Change-Id: I3b62b9aa0aecaf2957a445d82e966d52b9be6879