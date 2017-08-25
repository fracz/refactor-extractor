commit 0e589df9492b81af239a3ae803aceca6390ecddc
Author: Moshe Weitzman <weitzman@tejasa.com>
Date:   Tue May 10 03:33:57 2011 -0400

    #1151464 by moshe weitzman. Move define() from top of drush_testcase() to bottom so we can use is_windows() method there. Also change some Drush_TestCase:: to self:: for readability.