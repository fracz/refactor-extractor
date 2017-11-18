commit e2655f3c3834af8c68fc470987a5cabc6eb03ae0
Author: Trustin Lee <trustin@gmail.com>
Date:   Mon Aug 1 03:39:06 2011 +0900

    NETTY-389 java.lang.ClassCastException: org.jboss.netty.channel.FileRegion cannot be cast to org.jboss.netty.buffer.ChannelBuffer

    * Updated JavaDoc to let users know that not all transports support FileRegion
    * Added FIXME for future improvement