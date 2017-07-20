commit 4b7f5a728a6f44e08cd84c1d6333cf413b42361c
Author: Carsten Brandt <mail@cebe.cc>
Date:   Fri Aug 30 16:52:33 2013 +0200

    improved control over and handling of file and dir permissions

    - rename FileHelper::mkdir for API consistency
    - changed default permission for directories to 775 instead of 777
    - added some properties to classes that deal with files to allow control
      over directory permissions.