commit 0682421ce1e48c5009570e857385e1e280aa0dd3
Author: Trustin Lee <trustin@gmail.com>
Date:   Tue May 1 18:23:59 2012 +0900

    Remove unused classes

    - ChannelPipelineFactory will be replaced with sometime else when I
      refactory the bootstrap package
    - FileRegion is going away.  A user can deregister a channel and perform
      such operations by him/herself.  If this turns out to be too
      difficult, I'll introduce a new 'sendfile' operation to the outbound
      handler.