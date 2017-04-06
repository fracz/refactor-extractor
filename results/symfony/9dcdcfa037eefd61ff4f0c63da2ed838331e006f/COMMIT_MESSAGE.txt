commit 9dcdcfa037eefd61ff4f0c63da2ed838331e006f
Merge: 06da573 746170b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jan 25 16:06:27 2012 +0100

    merged branch lstrojny/form/patch-method-support (PR #3186)

    Commits
    -------

    746170b Make method non static
    c3f637b PATCH support and tests for DELETE support
    2dd4bf1 Support for PATCH method in forms

    Discussion
    ----------

    [form] Support for PATCH method in forms

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    Adds support for PATCH method. Accidentally I refactored the test RequestTest a bit to verify PUT, DELETE and PATCH and not only PUT.

    ---------------------------------------------------------------------------

    by lanthaler at 2012-01-25T13:26:26Z

    Why do you classify this as something that breaks backwards compatibility? I don't think it breaks anything.

    ---------------------------------------------------------------------------

    by lstrojny at 2012-01-25T13:28:28Z

    It’s not that relevant here, but before using patch throws an exception which it no longer does.

    ---------------------------------------------------------------------------

    by lanthaler at 2012-01-25T13:31:35Z

    Oh OK..

    ---------------------------------------------------------------------------

    by vicb at 2012-01-25T13:36:39Z

    Maybe you can update the [DomCrawler](https://github.com/symfony/symfony/blob/master/src/Symfony/Component/DomCrawler/Form.php) at the same time ?

    ---------------------------------------------------------------------------

    by lstrojny at 2012-01-25T13:55:21Z

    Done