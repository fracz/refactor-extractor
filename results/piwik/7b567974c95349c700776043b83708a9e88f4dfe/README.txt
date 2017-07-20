commit 7b567974c95349c700776043b83708a9e88f4dfe
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Sep 17 03:55:27 2012 +0000

    Fixes #3378
    Add call to Controller->setDate() to ensure the date used by the calendar is the same as the date used by API

    Also refactoring the standalone/index.tpl templates into 1 template since they were mostly duplicates

    git-svn-id: http://dev.piwik.org/svn/trunk@7006 59fd770c-687e-43c8-a1e3-f5a4ff64c105