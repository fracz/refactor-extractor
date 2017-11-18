commit 03b87a2f40c26948b7b0c9409c33ad44857218cf
Author: Christoph Studer <chstuder@google.com>
Date:   Wed Apr 30 17:33:27 2014 +0200

    Log notification clicks

    Emit notification_clicked log events when a notification
    is clicked from SystemUI.

    Also refactor the onNotificationClicked method to work with
    a key instead of individual notification params.

    Change-Id: Iffd15e95d46371b2ae7bfd00b2c348d9f4cf5d14