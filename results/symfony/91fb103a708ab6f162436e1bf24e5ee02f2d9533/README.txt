commit 91fb103a708ab6f162436e1bf24e5ee02f2d9533
Merge: f4c9c97 0a16cf2
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 8 20:50:54 2014 +0200

    minor #11844 [FrameworkBundle] improve handling router script paths (xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [FrameworkBundle] improve handling router script paths

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    The `server:run` command switches the working directory before starting the built-in web server. Therefore, the path to a custom router script had to be specified based on the document root path and not based on the user's working directory.

    Another option is to update the documentation (as started in symfony/symfony-docs#4194). Though I think the current behaviour is a bug. The intended behaviour can be derived from the command's help message:

    > ```
    If you have custom docroot directory layout, you can specify your own
     router script using --router option:

    >   ./app/console server:run --router=app/config/router.php
    ```

    As you can see, the path is specified based on the current working directory.

    Commits
    -------

    0a16cf2 improve handling router script paths