commit 128f0984184c18d3e4fc4062aa61482d8a87d22e
Author: skodak <skodak>
Date:   Tue Oct 9 12:49:54 2007 +0000

    MDL-11640
    * improved handling of dirty contexts in general - caching, switching in cron, marking, loading, etc.
    * role_assing() and role_unassign() now marks dirty contexts because we use has_capability() in this function - we can not do it later outside to speedup bulk operations
    * fixed some inline docs
    * fixed notice from rs closing
    * removed cached $CONTEXT from has_capability() - $context is now required parameter; this was hiding serious bugs when context did not exist or ppl passed false as parameter
    * removed some ===, we can not use these on function parameters - we must support ints, strings, '', nulls, etc. - this could be a source of really dangerous bugs in future
    * some other improvements and fixes - documented inline