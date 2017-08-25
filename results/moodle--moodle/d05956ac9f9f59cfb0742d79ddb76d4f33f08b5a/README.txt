commit d05956ac9f9f59cfb0742d79ddb76d4f33f08b5a
Author: moodler <moodler>
Date:   Wed Jan 14 11:50:29 2004 +0000

    Some improvements in efficiency of Recent Activity.

    There is now a new field in forum_discussions which has the userid
    of the author in it.  This saves a lookup every time to forum_posts.

    There is also some caching and rearrangement of the logic.

    It seems to work OK, I'm about to do some speed tests on moodle.org