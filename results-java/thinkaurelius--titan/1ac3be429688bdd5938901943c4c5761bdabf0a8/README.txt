commit 1ac3be429688bdd5938901943c4c5761bdabf0a8
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Thu May 10 13:06:38 2012 -0700

    Major refactoring of Configuration management and diskstorage interfaces:
    - Configuration is read from properties files using comons Configuration
    - Storage backend specific configuration is removed. StorageManager constructor is assumed to take Configuration as argument and loaded dynamically using reflection
    - getSlice method makes the assumption that startColumn is inclusive and endColumn is exclusive thereby simplifying the interface
    - openDatabase returns ordered database, openOrderedDatabase removed (in StorageManager)
    - added getIDBlock method to StorageManager (through interface IDAuthority). This is used to manage the id pool inside Titan

    The last change required a re-engineering of the internal id pool managed. The old LocalID was deprecated and a new IDPool interface as well as a StandardIDPool implementation added. Also, a dummy LocalIDManager was added to implement getIDBlock for Cassandra and Hbase - this needs to be replaced because it only works locally.

    THIS IS AN INTERMEDIATE COMMIT. The test cases have yet to be refactored.