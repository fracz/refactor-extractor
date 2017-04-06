commit 35233564fdc6d455f4492085c18038e76b1ae1b6
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Jul 7 01:26:40 2012 +0200

    buffer management refactoring
    First phase at improving buffer management and reducing even further buffer copies. Introduce a BytesReference abstraction, allowing to more easily slice and "read/write references" from streams. This is the foundation for later using it to create smarter buffers on top of composite netty channels for example (which http now produces) as well as reducing buffer copies when sending transport/rest responses.