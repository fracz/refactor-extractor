commit 3405aee2abf990d1f6078eb27da1b0035ab34769
Author: JongYoonLim <seedengine@gmail.com>
Date:   Thu Mar 19 14:25:19 2015 +0900

    Returns after encoding each message not do check following instance types

    Motivation:
    Current AbstractMemcacheObjectEncoder does unnecessary message type checking if the message is MemcacheMessage type.

    Modifications:
    Returns after encoding MemcacheMessage message.

    Result:
    Small performance improvement for this encoder.