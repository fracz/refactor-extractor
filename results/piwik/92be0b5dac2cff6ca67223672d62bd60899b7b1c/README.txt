commit 92be0b5dac2cff6ca67223672d62bd60899b7b1c
Author: sgiehl <stefangiehl@gmail.com>
Date:   Wed Jul 25 21:55:11 2012 +0000

    refs #3227 added first versions of some integration tests. there are still many things to be improved. in order to make them running correct the HTTP_HOST config needs to be changed in phpunit.xml. some of the tests aren't running because of encoding problems. I'll maybe have a look at that later

    git-svn-id: http://dev.piwik.org/svn/trunk@6560 59fd770c-687e-43c8-a1e3-f5a4ff64c105