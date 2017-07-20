commit 03cb077a32fcb55bd093c38bed134767ea92a8ce
Author: serbanghita@gmail.com <serbanghita@gmail.com@4c3b34b8-4606-11de-b695-a305669b6508>
Date:   Fri Aug 3 02:16:06 2012 +0000

    - refactored the code to work faster
    - added support for Medion tablets
    - added support for Arnova tablets
    - added alternative method $detect->is('AndroidOS') equivalent to $detect->isAndroidOS();
    - added posibility of using the method in batch mode. Now accepting custom $userAgent and $httpHeaders via isMobile($userAgent = null, $httpHeaders = null) and is($key, $userAgent = null, $httpHeaders = null) methods
    - added batch tests.php file

    git-svn-id: https://php-mobile-detect.googlecode.com/svn/trunk@51 4c3b34b8-4606-11de-b695-a305669b6508