commit 79cc389f1284e01c3c56244aec1d0b06522ed753
Author: adrian <adrian@1337.no-reply.drupal.org>
Date:   Tue Apr 14 04:49:03 2009 +0000

    Nicer status command, using an improved _core_site_credentials. Refactor pm slightly to use the new drush_bootstrap_max which bootstraps to the highest possible phase without generating any errors. This is also used by the status command now