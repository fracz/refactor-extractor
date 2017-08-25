commit 68a26730b46c9a87c1c76c297bc2c752f736be0e
Author: Anton Serdyuk <anton.serdyuk@gmail.com>
Date:   Fri Feb 1 23:17:38 2013 +0300

    add possibility to work with NO_DELAY sockets

    Using sockets without NO_DELAY option causes extreme lack of
    performance in some cases (for example RPC calls and responses).

    There is no possibility to set NO_DELAY option when working with streams, so
    AMQPSocketConnection class was added with some minor refactoring

    See benchmark for performance comparison between socket and stream connections