commit 092b5dde62df60c268c8d99bd5d45d50cda42acc
Merge: 55f682c 3eb67fc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jun 19 17:16:22 2012 +0200

    merged branch gajdaw/finder_current_fix (PR #4335)

    Commits
    -------

    3eb67fc [2.1][Component][Finder] $this->current() fix

    Discussion
    ----------

    [2.1][Component][Finder] $this->current() fix

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/gajdaw/symfony.png?branch=master)](http://travis-ci.org/gajdaw/symfony)
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT

    One method to resolve `->in("ftp://...")` problem is to create `RecursiveDirectoryFtpIterator`.
    (Details: [issue 3585](https://github.com/symfony/symfony/issues/3585))

    I think that all filters should access the information about current item calling `current()` or `getInnerIterator()`. Otherwise it will not work if we replace `RecursiveDirectoryIterator` with ftp iterator inside `Finder`.

    I'm not sure if that should go to 2.0 or 2.1 branch.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T09:20:19Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1373361) (merged 9f247921 into 58b92453).

    ---------------------------------------------------------------------------

    by gajdaw at 2012-05-19T10:51:10Z

    Probably it should go to master branch, because it improves commit done to master:

    https://github.com/symfony/symfony/commit/f2fea974600c9860d17614b001058253cba3bbe4

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T11:26:14Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1373982) (merged f9d1db8c into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T11:51:25Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1374031) (merged f1b4b4f7 into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T12:48:17Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1374303) (merged b6d073da into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T13:28:18Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1374568) (merged fd144c96 into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T13:35:38Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1374609) (merged 89a8d851 into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T04:31:46Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1385764) (merged 0d5b8322 into 58b92453).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T07:21:56Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1386545) (merged 3eb67fca into 1407f112).

    ---------------------------------------------------------------------------

    by stof at 2012-06-09T13:24:14Z

    seems good