commit 09db99bbd0429d14066a61be411b834247520c17
Author: joce <jocelyn.Fournier@gmail.com>
Date:   Mon Feb 1 10:02:47 2016 +0100

    [*] CORE : Quick fix to improve CLDR performance. Avoid to keep calling ICanBoogie\CLDR\FileProvider::retrieve which is quite heavy.