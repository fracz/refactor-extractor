commit a81e2c56e6e6e06fd4bd89c6fdba5c047c8e0b8d
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Apr 20 00:55:46 2012 +0000

    Fixed issues 798 and 799:
    - new binary protocol version with huge refactoring of addCluster, addDataSegment and dropDataSegment
    - strong refactoring to clean the code behind cluster management
    - created new OClusterFactory to be used from Orient class
    - deprecated Logical cluster (yeah!)
    - new ODatabase.getSize()