commit 14a3009de52a485d7860873f474e1cfed7aa8630
Author: lvca <l.garulli@gmail.com>
Date:   Wed Sep 18 02:27:54 2013 +0200

    Fixed most of synchronization problems with topics/queues, improved performance and increase concurrency (no more thread pool!), supported hot-cfg of servers in configuration file, always use node name rather than id