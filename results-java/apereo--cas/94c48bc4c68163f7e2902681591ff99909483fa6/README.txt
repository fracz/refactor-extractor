commit 94c48bc4c68163f7e2902681591ff99909483fa6
Author: William G. Thompson <wgthom@gmail.com>
Date:   Thu Aug 11 15:33:52 2011 +0000

    CAS-1027

    improved property names and debug messaging.  added more comments about the throttling issue.
    throttling needs to be looked.  A collision of recently-used-ticket/cool-down and the registry cleaner could result in an otherwise valid ticket being deleted.