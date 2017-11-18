commit 50ab63f5831fed5cfa888fb67f0a27eb4c0a86c4
Author: Dan Morrill <morrildl@google.com>
Date:   Fri Mar 5 16:16:19 2010 -0800

    Adding definitions & APIs for features that are newly-optional in FroYo.
    This also refactors the files containing the features so that they are more
    modular. Note that this also changes data/etc/Android.mk so that
    required_hardware.xml is NOT copied automatically for all devices
    anymore. Accordingly, that file is removed.