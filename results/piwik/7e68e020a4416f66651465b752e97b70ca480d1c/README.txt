commit 7e68e020a4416f66651465b752e97b70ca480d1c
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Aug 19 01:21:22 2008 +0000

    Fixed the menu is now fully translatable! ouf
    Fixed javascript handling of translations: we can now load translations strings in several places in the templates, they will all be concatenated into one bigger object
    Fixed handling japanese error messages in API with correct charset
    Finished (hopefuly?) to make all strings translatable.
    Added colored logo now in login screen
    All templates now defined the dates necessary to build the calendar, as we notice that most pages want to show the calendar.
    Added destructor to LogStats/Db.php so that plugins don't have to call disconnect() on the object
    Made JS tag smaller
    Improved templates structuring, refactored CSS, etc. still lots to do in this area, if you wanna help?


    git-svn-id: http://dev.piwik.org/svn/trunk@616 59fd770c-687e-43c8-a1e3-f5a4ff64c105