commit 988707a61cd5d0ed3614652ad85c182f4e35a00e
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue Jun 20 07:33:17 2000 +0000

    Here we go again with a rather large commit:
    fixed a lot of annoying bugs and boxed whatever there was left to be boxed.

     * user.class.php: renamed $user->update() to $user->rehash().
     * user.class.php: fixed a typical quote-bug in $user->rehash().
     * functions.inc: fixed bug in displayOldHeadlines().
     * functions.inc: improved several functions.
     * account.php: fixed major bug in showUser().
     * account.php: added some extra words to the human-readable
                    password-generator(tm).
     * account.php: boxed ALL functions! Fieuw!
     * submit.php: add some general information and guidlines on how to
                   post submissions.
     * config.inc: re-thought the categories to be more generic.
     * submission.php: minor changes
     * search.pph: fixed minor bug with the author's names.

    Woops.  I have an exam within 4 hours: back to my books. ;-)

    --------------------------------------------------------------------

     * Anyone could check sumbit.php, sumbission.php and faq.php for
       typoes?
     * Anyone could adjust calendar.class.php to fit IE?  *huh*huh*
     * Don't be scared to hack along (see below)!  I'll be working on
       the submissions and comments.

    --------------------------------------------------------------------

    Status of drop v0.10:
    (make the system erational' and release it.)

      - submissions:
          submission queue         (75% complete)
          submission moderation    (75% complete)
      - comments:
          comment moderation       ( 0% complete)
          comment administration   ( 0% complete)
          fixup timestamp mess     ( 0% complete)
      - user system:
          mail password            ( 0% complete)
          user administation       (50% complete)
          patch admin.php          ( 0% complete)
          account confirmation     ( 0% complete)
          e-mail confimation upon modification of e-mail address
                                   ( 0% complete)
      - proper handling of forms: text2html, html2text
          html2txt, txt2html       (10% complete)
          bad-word filter          (80% complete)
          automatic link detection ( 0% complete)
          allowed HTML-tag checker ( 0% complete)
      - FAQ:
          cleanup, disclaimer      (50% complete)
      - theme:
          box everything          (100% complete)