commit 0cd189bdf348b7ce60e32179e31575bedd2a62b5
Author: Petr Skoda <commits@skodak.org>
Date:   Tue Apr 22 10:36:56 2014 +0800

    MDL-41185 fix url in course_viewed event after course deleted

    Includes a minor fix for course invalidation after deleting of course,
    this improvement should not affect normal execution, that is why
    it was not backported.