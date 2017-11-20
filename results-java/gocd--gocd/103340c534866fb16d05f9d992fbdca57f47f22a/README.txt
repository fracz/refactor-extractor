commit 103340c534866fb16d05f9d992fbdca57f47f22a
Author: Ketan Padegaonkar <KetanPadegaonkar@gmail.com>
Date:   Wed Apr 13 13:22:03 2016 +0530

    Upgrade all agent-server communication to use HTTPS by default.

    * Currently defaults to using HTTPs without peer verification
    * Users may optionally set -sslVerificationMode to improve
      security from NONE to NO_VERIFY_HOST to FULL
    * Upgrade from commons httpclient 3.0.1 to httpcomponents 4.5.2