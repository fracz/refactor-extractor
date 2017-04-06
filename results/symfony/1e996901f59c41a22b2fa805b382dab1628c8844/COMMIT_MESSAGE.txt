commit 1e996901f59c41a22b2fa805b382dab1628c8844
Merge: a972770 b67a1dd
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Mar 6 10:33:18 2011 +0100

    Merge remote branch 'schmittjoh/security'

    * schmittjoh/security:
      [Security] forward the entire access denied exception instead of only the message
      [Security] changed defaults for MessageDigestEncoder
      TICKET #9557: session isn't required when using http basic authentification mecanism for example
      [Security] improved entropy to make collision attacks harder
      [Security] added the 'key' attribute of RememberMeToken to serialized string to be stored in session
      Fix the Acl schema generator script.