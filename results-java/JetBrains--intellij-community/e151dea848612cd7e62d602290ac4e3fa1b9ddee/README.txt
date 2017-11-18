commit e151dea848612cd7e62d602290ac4e3fa1b9ddee
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Apr 21 19:21:54 2017 +0300

    Unify and improve notifications after Update Project and after Push

    * Use standard notifications instead of toolwindow ones.
    * IDEA-171836 Don't auto-focus the Update Info tab:
      provide a link to focus it on demand instead.
    * Always attach the updated info after push (without focusing),
      otherwise it was required to seek through the Event Log to view
      updated files.
    * Keep the auto-focus functionality for other clients for compatibility:
      they require much more work to do to rewrite their notifications.

    * Mark showUpdateInfo nullable and fix NPEs in several places.