commit 9be82d942fc6ab2772197c84a35a4c374c604cbc
Author: Misko Hevery <misko@hevery.com>
Date:   Mon Jun 4 15:06:02 2012 -0700

    refactor($compile): always call attr.$observe

    attr.$observe used to call function only if there was interpolation
    on that attribute. We now call the observation function all the time
    but we only save the reference to it if interpolation is present.