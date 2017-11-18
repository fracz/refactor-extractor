commit d9ae33d4c0473ef53a9ea560536467333136c0a0
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Fri Oct 30 08:53:16 2015 -0700

    KAFKA-2711; SaslClientAuthenticator no longer needs KerberosNameParser in constructor

    Also refactor `KerberosNameParser` and `KerberosName` to make the code
    clearer and easier to use when `shortName` is not needed.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jun Rao <junrao@gmail.com>

    Closes #390 from ijuma/kafka-2711