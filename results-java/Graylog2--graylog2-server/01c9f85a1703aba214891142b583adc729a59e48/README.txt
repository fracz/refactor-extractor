commit 01c9f85a1703aba214891142b583adc729a59e48
Author: Kay Roepke <kay.roepke@xing.com>
Date:   Mon Apr 30 15:49:21 2012 +0200

    more cleanup

    removed checked exceptions via 'new Exception' and replace them with IllegalStateExceptions

    refactored GELFChunkManager to accept a GELFMessage, which then gets converted to a GELFMessageChunk via its new constructor.
    made the message payload publicly readable
    removed GELFMessage.asChunk()