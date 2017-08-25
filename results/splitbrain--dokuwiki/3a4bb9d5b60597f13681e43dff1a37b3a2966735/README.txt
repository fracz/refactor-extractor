commit 3a4bb9d5b60597f13681e43dff1a37b3a2966735
Author: Chris Smith <chris.eureka@jalakai.co.uk>
Date:   Mon Apr 27 01:13:46 2009 +0200

    FS#1680 - improve email address validation in config plugin

    - setting_email and setting_richemail updated to use mail_isvalid() from inc/mail.php
    - _pattern improved if any plugin extends either class for its own settings (this maybe
      over cautious. Its probably very unlikely that any plugin does this).

    darcs-hash:20090426231346-f07c6-2af83d890ff4d92b14637ef6024d3fb68ba97efd.gz