commit 52a916b5b21878eec2b43bd4258ebf69e4416840
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Thu Nov 7 13:11:27 2013 +0100

    Reenables testing of HeartbeatContext. Adds test for a bug and a fix for it.

    A problem which is sometimes encountered is that an instance may enter the cluster,
     suspect an instance, send that suspicion everywhere and then exit. All remaining instances
     however will keep the suspicion in their state, which leads to errors, such as instances
     being marked as failed while they are not. This is now tested against and fixed.
    A further improvement on this should be the purging of stale entries on instance exit,
     but that can come later.