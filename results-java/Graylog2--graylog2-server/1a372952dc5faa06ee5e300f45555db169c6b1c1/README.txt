commit 1a372952dc5faa06ee5e300f45555db169c6b1c1
Author: Jochen Schalanda <jochen@schalanda.name>
Date:   Sun Aug 14 19:22:13 2011 +0200

    Refactored command line argument and configuration file handling
    * Improved and refactored Configuration class
    * Added extensive unit test for Configuration class
    * Tried to reduce tight coupling with Main and Configuration class
    * addresses issues SERVER-43, SERVER-44