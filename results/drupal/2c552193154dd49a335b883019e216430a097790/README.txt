commit 2c552193154dd49a335b883019e216430a097790
Author: Angie Byron <webchick@24967.no-reply.drupal.org>
Date:   Mon Aug 24 00:14:23 2009 +0000

    #497118 by chx, catch, pwolanin, JoshuaRogers, and Jacob Singh: Remove the function registry. While the hope was that this would result in improved performance for low-end hosts, it comes at the expense of critical development experience problems and less benefit than something like APC. Class registry remains intact to facilitate autoloading.