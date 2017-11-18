commit 9ae040471bfec561f60fb410c03ffb2939c7ab87
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Sep 13 10:15:24 2011 +0200

    GRADLE-1747 - Wanted to check-in some native work dir stuff I had shelved last week. First attempt to change the process working dir. Not yet validated on windows. More details:
    - refactored the Kernel32 into a top-level class from WindowsProcessStarter because the former has other reasons for changing. Updates of work dir for windows is a work in progress - currently I'm using Kernel32 but I could use _chdir