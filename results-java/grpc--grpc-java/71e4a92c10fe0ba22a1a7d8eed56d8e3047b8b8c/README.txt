commit 71e4a92c10fe0ba22a1a7d8eed56d8e3047b8b8c
Author: lryan <lryan@google.com>
Date:   Thu Sep 25 18:25:54 2014 -0700

    First steps in reducing dependency on proto from runtime.

    - Remove transport.proto and move status codes into Status.java with a little refactoring to make
    status easier & more precise to use
    - Move DeferredProtoInputStream into a proto subpackage
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=76392172