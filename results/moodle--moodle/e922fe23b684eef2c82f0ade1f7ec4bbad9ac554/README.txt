commit e922fe23b684eef2c82f0ade1f7ec4bbad9ac554
Author: Petr Skoda <commits@skodak.org>
Date:   Fri Oct 14 12:48:00 2011 +0200

    MDL-29602 accesslib improvements

    Refactoring and improvements of the accesslib.php library including prevention of access for not-logged-in users when forcelogin enabled, improved context caching, OOP refactoring of contexts, fixed context loading, deduplication of role definitions in user sessions, installation improvements, decoupling of enrolment checking from capability loading, added detection of deleted and non-existent users in has_capability(), new function accesslib test, auth and enrol upgrade notes.

    More details are available in tracker subtasks.