commit dd429b249504b8f7c3caa3bcd417696839c6d328
Author: Idel Pivnitskiy <Idel.Pivnitskiy@gmail.com>
Date:   Sun Jul 20 00:40:45 2014 +0400

    Small fixes and improvements

    Motivation:

    Fix some typos in Netty.

    Modifications:

    - Fix potentially dangerous use of non-short-circuit logic in Recycler.transfer(Stack<?>).
    - Removed double 'the the' in javadoc of EmbeddedChannel.
    - Write to log an exception message if we can not get SOMAXCONN in the NetUtil's static block.