commit 7f79e22eb4baa44d17d34beddae48b1bfd18eaff
Author: alecpl <alec@alec.pl>
Date:   Wed Nov 30 10:48:17 2011 +0000

    - Set sizelimit of main search function for vlv_search to page_size.
      It was requested as performance improvement, but I wasn't able to confirm this. However it doesn't break anything.