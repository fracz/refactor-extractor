commit 3fe676a3ddc81add4a64a0aed6d0551ff34732f2
Author: Rhodey Orbits <rhodey@anhonesteffort.org>
Date:   Thu Apr 23 12:32:43 2015 -0700

    improve pdu generation in SmsListenerTest

    one Android 2.3.3 device was throwing NPEs while parsing the precompiled
    pdu test vectors so I modified SmsListenerTest to generate them on the fly
    using Android's own PDU related classes. All tests now pass on 2.3.3,
    4.2.2, 4.4.4, 5.1.

    Closes #3048

    // FREEBIE