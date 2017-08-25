commit cae945d272cc8394bf6c539b150f4db56190a629
Author: Dan Poltawski <dan@moodle.com>
Date:   Tue Aug 19 11:26:01 2014 +0100

    MDL-46880 mod_forum: move cron to scheduled task

    Note that this is a very basic conversion without doing any refactoring
    to split the tasks up better. That will come in MDL-44734, this is about
    being a safe backportable change to give admins better control over the
    running of the forum cron task.