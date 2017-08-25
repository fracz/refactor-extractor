commit 4b734e74ae26144a8725e7a4e9cdefa8d54d0583
Author: Petr Škoda <commits@skodak.org>
Date:   Sat Jul 6 14:35:02 2013 +0200

    MDL-39846 more fixing and cleanup of new events

    * fix typos (thanks Rajesh)
    * move log related data out from event API specification
    * change callable key in definition to callback
    * use event data in observers instead of cached records if possible
    * event level is now a number 1…100
    * improved event safety checks
    * add event name and description method
    * new can_view() method
    * improve unit tests to test all callable types
    * improved performance