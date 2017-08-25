commit 2ca77e08d8dae3f81a435b9775be1fbd0e3ddc9a
Author: andrey <andrey@localhost>
Date:   Thu Mar 15 21:28:24 2001 +0000

    * Includes are now always done via generated function call to protect
      namespace.
    * config_load now always uses global config object to improve
      performance.