commit 0ed984be3888ffbc84b6a50e3fb947ba89ae9ff8
Author: Adam Murdoch <adam.murdoch@gradleware.com>
Date:   Tue Oct 22 12:21:34 2013 +1100

    Some refactoring of the FileLockAccess classes:

    - StateInfoProtocol works only with DataInput and DataOutput, so that it is responsible only for serialization.
    - Use buffered reads and writes to read/write the state region contents.
    - Read or write the state region exactly once when opening the lock.