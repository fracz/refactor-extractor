commit 8e97c63fbc29417bc2dbdad02a1eaa7cf018e8ed
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Sep 30 11:59:58 2014 +0200

    Logs progress catching up to master in HA

    Possible after a refactoring where the response unpacker now accepts a
    TxHandler in its accept method. This makes it possible to reuse a single
    response unpacker for multiple TxHandler purposes, and by extension a
    single MasterClient instance for multiple purposes.