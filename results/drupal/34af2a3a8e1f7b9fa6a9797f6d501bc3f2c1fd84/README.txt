commit 34af2a3a8e1f7b9fa6a9797f6d501bc3f2c1fd84
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat May 19 13:41:52 2001 +0000

    CHANGES:

    - Rewrote the cron system.  Removed cron.module and moved all cron
      related options to settings.module.  Cron was a confusing thing:
      it has been made simpler both in terms of code and configuration.

       + You had to rehash your modules to make the cron show up in
         the list.  This is no longer required.

       + You couldn't tell what cron "watchdog" or cron "story" were
         up to.  Instead, we now display a clear description message
         for every cron involved.

       + The user interface of setting.module - and the admin section
         in general, looks a bit ackward but I couldn't care less and
         don't want to see this improve at the time being.

    - Improved setting.module:
       + Now uses variable_set().
       + Added some help and documentaition on how to setup cron.

    - Improved ./export.

    - Updated CHANGELOG.


    TODO:

    - I'm now going to look into UnConeD's question with regard to
      check_output() and $theme->node(), as well as the filter and
      macro stuff.  I'll probably be fine-tuning setting.module a
      bit more on my way.