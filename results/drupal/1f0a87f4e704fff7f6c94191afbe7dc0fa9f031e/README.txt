commit 1f0a87f4e704fff7f6c94191afbe7dc0fa9f031e
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Sep 11 07:45:22 2000 +0000

    Over the last 2 days I redid and reorganized an afwul lot of code and
    made quite a lot of additions.   The most remarkable addition is the
    diary server, which I slapped together in less then 40 minutes.   Most
    of the other changes are however `unvisible' for the user but add much
    value to a better maintainability from a developer's objective.  Like
    always, I fixed quite a number of small bugs that creeped into the code
    so we should have a bigger, better and more stable drop.org.

    Unfortunatly, some theme update _are_ required:


    REQUIRED THEME UPDATES:
    =======================

    * use format_username() where usernames are used
    * use format_date() where timestamps/dates are used
    * use format_email() where e-mail addresses are displayed
    * use format_url() where url are displayed
    * replace 'formatTimestamp' with format_date
    * replace 'morelink_*' with 'display_morelink'

    [most of these functions are in function.inc or template.inc]

    ___PLEASE___ (<- this should get your attention ;) update your themes
    as soon as possible - it only takes 30 min. to get in sync with the
    other themes.  Don't start whining about the fact you don't know what
    to change ... either eat the source cookie, or ask me to elaborate on
    a few changes.  Just let me know what's puzzling you and I'll try to
    help you out!


    TODO LIST FOR NEXT WEEK
    =======================
    * Add checks for max. text length in textarea's?  Is there an HMTL
      attribute for this or ...?
    * Comment moderation + mojo
    * Edit/admin user accounts: block, delete, change permissions, ...
    * E-mail password, change password, change e-mail address -> extra
      checks and routines to validate such `special' changes.
    * Input checking - input filter: bad words, html tags, ...