commit 790c6980fee4b967979e52d6b8ddff9f85eafb5b
Author: Rhodey Orbits <rhodey@anhonesteffort.org>
Date:   Tue Apr 21 17:36:00 2015 -0700

    improved challenge sms verification in SmsListener

    some carriers prepend or append arbitrary text to sms messages
    enroute, SmsListener failed to handle the append case.

    Fixes #2919
    Closes #3032
    // FREEBIE