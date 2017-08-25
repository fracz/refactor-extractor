commit d5a8225c0e818b95cf06a4afcdb07d4cf5ecf08b
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Fri Mar 6 00:16:17 2015 +0100

    Fix totally broken AppStore code...

    As it turned out the AppStore code was completely broken when it came from apps delivered from the appstore, this meant:

    1. You could not disable and then re-enable an application that was installed from the AppStore. It simply failed hard.
    2. You could not disable apps from the categories but only from the "Activated" page
    3. It did not show the activation state from any category page

    This code is completely static and thus testing it is impossible. We really have to stop with "let's add yet another feature in already existing static code". Such stuff has to get refactored first.

    That said, this code works from what I can say when clicking around in the AppStore page GUI. However, it may easily be that it does not work with updates or whatsever as I have no chance to test that since the AppStore code is not open-source and it is impossible to write unit-tests for that.

    Fixes https://github.com/owncloud/core/issues/14711