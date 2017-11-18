commit 959eabb48bfcdce350f2f91ce86fa0dce1fbd72a
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Thu Feb 13 17:07:20 2014 +0400

    [log] filters refactoring

    * Store all filters inside the VcsLogFilterCollection;
      Use single filter of each type: if a filter accepts several values
      (like branches or users), encapsulate this into this filter class.

      => Now a single object is passed instead of passing several
         different Collections.
      => It allows to define the filters interplay strategy in one
         place instead of adding proper comments in all necessary places.
      => It allows to define interplay of filters of the same type in the
         filter class instead of exposing it everywhere.

    * VcsLogGraphFilter, VcsLogDetailsFilter & correspondent methods from
      the VcsLogFilterCollection are moved to vcs-log-api which is not
      good, but is made to simplify this fix. It will be improved later,
      after some related refactoring in the Graph.

    * Since matching & user name identification has moved into the
      VcsLogUserFilterImpl, it fixes IDEA-119215: "me" is now being
      understood by the filter and properly expanded.