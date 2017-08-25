commit e66e6c7ed1b39ab15b49c3601890ff63beb6a2f4
Author: chris <chris@jalakai.co.uk>
Date:   Sun Apr 30 17:23:08 2006 +0200

    improvements to common plugin i/f

    update comments to work with DokuWiki's auto generated API docs.

    slight restructure of configuration functions and comments
    - loadConfig now loads plugin config settings into
      $conf['plugin'][<plugin_name>] & $this->conf.  These are aliases
      ensuring only one copy is stored.
    - readDefaultSettings() reads the plugin's conf/default.php

    darcs-hash:20060430152308-9b6ab-9ec53e79ce5b07405acb84d19d81df9dd609612e.gz