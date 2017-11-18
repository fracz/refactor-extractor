commit 5927c9f1b12f597839a664c1c6593114175cbcd8
Author: Alex Klyubin <klyubin@google.com>
Date:   Fri Apr 10 13:28:03 2015 -0700

    Use JCA names for block modes, paddings, and digests.

    This replaces int-based enums from KeyStoreKeyConstraints with
    String values commonly used in JCA API.

    As part of under the hood refactoring:
    * KeyStoreKeyCharacteristics and KeyStoreKeyConstraints have been
      merged into KeyStoreKeyProperties.
    * KeymasterUtils methods operating on KeymasterArguments and
      KeymasterCharacteristics have been moved to their respective
      classes.

    Bug: 18088752
    Change-Id: I9c8b984cb3c28184adb617e34d87f2837bd1d3a1