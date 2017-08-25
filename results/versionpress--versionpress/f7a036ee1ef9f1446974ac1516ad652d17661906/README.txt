commit f7a036ee1ef9f1446974ac1516ad652d17661906
Author: Borek Bernard <borekb@gmail.com>
Date:   Fri Feb 13 19:16:33 2015 +0100

    [#292] Several improvements to system info page:

     - There is now a "quick summary" section which mostly points out some of the important info from other arrays
     - Git binary is now reported
     - 'wordpress-info' array contains less details about plugins and themes - things like descriptions are not very useful
     - 'php-info' section now lists all the available extensions in a better format
     - Fixed PHP warning in system-info.php if GET parameter was missing