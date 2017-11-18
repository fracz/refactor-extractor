commit cad5768e22e5132efb2e5179f4988a763c7a8d9e
Author: John Spurlock <jspurlock@google.com>
Date:   Sat Jul 26 17:09:56 2014 -0400

    Doze: Update tease signal to buzz-beep-blink from NoMan.

    Inform SystemUI when NoMan buzzes, beeps or blinks.  Use that
    as the notification signal when dozing instead of trying to figure
    out interesting panel content updates.

    At some point, we should move the entire calculation up into SystemUI
    itself, but that's too large of a refactoring to perform now.

    Bug:15863249
    Change-Id: I40e92334977e0676a1363774c2cbbf91d72ec8e5