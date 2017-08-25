commit 03cedd62834549ea3a47275e1d151c06d3753616
Author: skodak <skodak>
Date:   Wed Oct 10 12:19:27 2007 +0000

    MDL-11432 eliminated majority of RecordCount uses, added several missing rs_close() - this should help improve perf on some platforms - Eloy says ;-)