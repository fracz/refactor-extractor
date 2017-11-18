commit 0946638895d77e473f606de7214e808a9eab8ec0
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Mon Oct 19 16:24:53 2015 +0200

    Reuse netty buffers during backup

    During backup, bytes from store files are transferred using netty
    ChannelBuffers. Such buffers were created for each chunk of data that
    was send out. Default chunk size is 4M so for big stores amount of
    created buffers was huge.

    This commit introduces a chunking channel buffer that is able to reuse
    ChannelBuffers. Currently it is used only for backup because excessive
    buffer creation was only noticed there.

    BufferReusingChunkingChannelBuffer uses a queue of free buffers and
    subscribes to 'write completed' notifications with a listener that
    clears the used buffer and puts it on the queue of free buffers.
    Essentially this chunking channel buffer trades allocation of dynamic
    netty buffers for allocation of ChannelFutureListeners. This is a right
    thing to do because ChannelBuffer is an array based structure while
    ChannelFutureListener is only an anonymous class that captures a
    single ChannelBuffer.

    This change was tested on a 2.5GB store with full backups running in a
    tight loop. 1 minute JFR recording showed following results:
     * without buffer reuse: 14592 byte[] instances, total size 26.05GB
     * with buffer reuse: 2436 byte[] instances, total size 9.13GB

    So improvement in object allocation would probably be only visible for
    big stores and full backups.