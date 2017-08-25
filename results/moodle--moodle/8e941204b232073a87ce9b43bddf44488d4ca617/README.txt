commit 8e941204b232073a87ce9b43bddf44488d4ca617
Author: Petr Škoda <commits@skodak.org>
Date:   Sat Oct 13 21:43:28 2012 +0200

    MDL-35843 prepare fox expiry refactoring to core enrol feature

    AMOS BEGIN
      MOV [errorthresholdlow,enrol_manual],[errorthresholdlow,core_enrol]
      MOV [expirynotify,enrol_manual],[expirynotify,core_enrol]
      MOV [expirynotify_help,enrol_manual],[expirynotify_help,core_enrol]
      MOV [expirynotifyall,enrol_manual],[expirynotifyall,core_enrol]
      MOV [expirynotifyteacher,enrol_manual],[expirynotifyenroller,core_enrol]
      MOV [notifyhour,enrol_manual],[expirynotifyhour,core_enrol]
      MOV [expirythreshold,enrol_manual],[expirythreshold,core_enrol]
      MOV [expirythreshold_help,enrol_manual],[expirythreshold_help,core_enrol]
    AMOS END