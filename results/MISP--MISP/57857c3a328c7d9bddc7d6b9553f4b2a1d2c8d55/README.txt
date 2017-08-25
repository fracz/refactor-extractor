commit 57857c3a328c7d9bddc7d6b9553f4b2a1d2c8d55
Author: iglocska <andras.iklody@gmail.com>
Date:   Fri Jun 16 08:41:12 2017 +0200

    new: Performance improvements for the pub-sub modules

    - Only load and open connection to redis for the pub-sub connection once.
    - Massive performance boost when the ZMQ functionality is enabled