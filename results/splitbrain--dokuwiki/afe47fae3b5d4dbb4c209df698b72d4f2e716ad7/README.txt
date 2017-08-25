commit afe47fae3b5d4dbb4c209df698b72d4f2e716ad7
Author: Anika Henke <anika@selfthinker.org>
Date:   Sun Dec 5 13:01:15 2010 +0000

    made template functions more flexible

    * attention: incompatible to previous version!
    * introduced _tpl_action() (wrapper similar to tpl_action())
    * improved discussion and user page functions
      * made them work independent from config
      * added full control to how the page links are built (with placeholders @ID@ and @USER@)
    * config option changes: removed 'discussNSreverse', renamed 'discussionNS' and 'userNS' to 'discussionPage' and 'userPage'