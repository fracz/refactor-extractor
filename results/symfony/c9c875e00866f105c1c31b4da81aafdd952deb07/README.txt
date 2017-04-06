commit c9c875e00866f105c1c31b4da81aafdd952deb07
Merge: 209fb45 acf0075
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu May 12 13:43:28 2016 -0500

    minor #18714 [Cache] Clean RedisAdapter pipelining + FilesystemAdapter (nicolas-grekas)

    This PR was merged into the 3.1-dev branch.

    Discussion
    ----------

    [Cache] Clean RedisAdapter pipelining + FilesystemAdapter

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.1
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This PR refactorize Redis pipelining so that the case for handling RedisArray is abstracted, thus less bug prone.

    Commits
    -------

    acf0075 [Cache] Clean RedisAdapter pipelining + FilesystemAdapter