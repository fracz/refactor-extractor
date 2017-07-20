commit 0494a63c73790b3d62788e3f5f6eefca468ea22b
Author: robocoder <anthon.pang@gmail.com>
Date:   Thu Sep 17 23:29:27 2009 +0000

    fixes #904 - MySQL error codes; unsupported adapters can map these to driver-specific SQLSTATE (see example)
    fixes #980 - Piwik Installation support for "MySQL Improved" (mysqli) extension
    fixes #984 - Set client connection charset to utf8.

    Fixed tracker profiling data not recorded until after report generated.

    More refactoring and database abstraction:
     - Installation gets a list of adapters instead of hardcoding in the plugin
     - checking for database-specific system requirements deferred to the adapter
     - error detection moved to adapter but we still use MySQL error codes rather than defining new constants

    Note: unit tests don't run with MYSQLI -- Zend Framework's Mysqli adapater doesn't support prepare() yet



    git-svn-id: http://dev.piwik.org/svn/trunk@1473 59fd770c-687e-43c8-a1e3-f5a4ff64c105