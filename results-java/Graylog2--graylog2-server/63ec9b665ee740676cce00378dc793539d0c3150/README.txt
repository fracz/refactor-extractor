commit 63ec9b665ee740676cce00378dc793539d0c3150
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Aug 24 16:25:05 2015 +0200

    increase timeout for CSV export request to Long.MAX_VALUE

     - a value -1 will block browsers completely, which it shouldn't but it does.
     - backend support for cancelling the connection/scroll is not existant, so an overly long timeout will not make it worse
     - improve some debug output

    fixes Graylog2/graylog2-web-interface#1478