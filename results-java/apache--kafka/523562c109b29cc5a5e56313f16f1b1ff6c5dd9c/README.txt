commit 523562c109b29cc5a5e56313f16f1b1ff6c5dd9c
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Wed Jan 27 08:23:25 2016 -0800

    KAFKA-3152; kafka-acl doesn't allow space in principal name

    * Add quotes to `$` in shell scripts
    This is necessary for correct processing of quotes in the
    user command.

    * Minor improvements to AclCommand messages

    * Use a principal with a space in `SslEndToEndAuthorizationTest`
    This passed without any other changes, but good avoid regressions.

    * Clean-up `TestSslUtils`:
    Remove unused methods, fix unnecessary verbosity and don't set security.protocol (it should be done at a higher-level).

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Grant Henke <granthenke@gmail.com>, Jun Rao <junrao@gmail.com

    Closes #818 from ijuma/kafka-3152-kafka-acl-space-in-principal