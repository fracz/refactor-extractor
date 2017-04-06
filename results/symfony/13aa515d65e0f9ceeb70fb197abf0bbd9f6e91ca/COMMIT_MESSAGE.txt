commit 13aa515d65e0f9ceeb70fb197abf0bbd9f6e91ca
Merge: e7dbc38 100e97e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Apr 7 10:18:55 2012 +0200

    merged branch jakzal/FilesystemTests (PR #3811)

    Commits
    -------

    100e97e [Filesystem] Fixed warnings in makePathRelative().
    f5f5c21 [Filesystem] Fixed typos in the docblocks.
    d4243a2 [Filesystem] Fixed a bug in remove being unable to remove symlinks to unexisting file or directory.
    11a676d [Filesystem] Added unit tests for mirror method.
    8c94069 [Filesystem] Added unit tests for isAbsolutePath method.
    2ee4b88 [Filesystem] Added unit tests for makePathRelative method.
    21860cb [Filesystem] Added unit tests for symlink method.
    a041feb [Filesystem] Added unit tests for rename method.
    8071859 [Filesystem] Added unit tests for chmod method.
    bba0080 [Filesystem] Added unit tests for remove method.
    8e861b7 [Filesystem] Introduced workspace directory to limit complexity of tests.
    a91e200 [Filesystem] Added unit tests for touch method.
    7e297db [Filesystem] Added unit tests for mkdir method.
    6ac5486 [Filesystem] Added unit tests for copy method.
    1c833e7 [Filesystem] Added missing docblock comment.

    Discussion
    ----------

    [Filesystem] Fixed a bug in remove() being unable to unlink broken symlinks

    While working on test coverage for Filesystem class I discovered a bug in remove() method.

    Before removing a file a check is made if it exists:

        if (!file_exists($file)) {
            continue;
        }

    Problem is [file_exists()](http://php.net/file_exists) returns false if link's target file doesn't exist. Therefore remove() will fail to delete a directory containing a broken link. Solution is to handle links a bit different:

        if (!file_exists($file) && !is_link($file)) {
            continue;
        }

    Additionally, this PR improves test coverage of Filesystem component.

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes

    ---------------------------------------------------------------------------

    by cordoval at 2012-04-07T00:55:59Z

    ✌.|•͡˘‿•͡˘|.✌

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-07T06:12:34Z

    Tests do not pass for me:

        PHPUnit 3.6.10 by Sebastian Bergmann.

        Configuration read from /Users/fabien/work/symfony/git/symfony/phpunit.xml.dist

        .........................EE.......

        Time: 0 seconds, Memory: 5.25Mb

        There were 2 errors:

        1) Symfony\Component\Filesystem\Tests\FilesystemTest::testMakePathRelative with data set #0 ('/var/lib/symfony/src/Symfony/', '/var/lib/symfony/src/Symfony/Component', '../')
        Uninitialized string offset: 29

        .../rc/Symfony/Component/Filesystem/Filesystem.php:183
        .../rc/Symfony/Component/Filesystem/Tests/FilesystemTest.php:434

        2) Symfony\Component\Filesystem\Tests\FilesystemTest::testMakePathRelative with data set #1 ('var/lib/symfony/', 'var/lib/symfony/src/Symfony/Component', '../../../')
        Uninitialized string offset: 16

        .../rc/Symfony/Component/Filesystem/Filesystem.php:183
        .../rc/Symfony/Component/Filesystem/Tests/FilesystemTest.php:434

        FAILURES!
        Tests: 34, Assertions: 67, Errors: 2.

    ---------------------------------------------------------------------------

    by jakzal at 2012-04-07T07:26:15Z

    Sorry for this. For some reason my PHP error reporting level was to low to catch this...

    Should be fixed now but I needed to modify the makePathRelative() (this bug existed before).