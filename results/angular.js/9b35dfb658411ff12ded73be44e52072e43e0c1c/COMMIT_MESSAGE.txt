commit 9b35dfb658411ff12ded73be44e52072e43e0c1c
Author: Shahar Talmi <shahar.talmi@gmail.com>
Date:   Mon Mar 2 22:16:52 2015 +0200

    refactor($browser): remove private polling mechanism

    The only feature of Angular using this mechanism was `$cookies`,
    which no longer mirrors the browser cookie values and so does not
    need to poll.

    Closes #11222