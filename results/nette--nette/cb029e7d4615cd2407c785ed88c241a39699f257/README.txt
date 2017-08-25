commit cb029e7d4615cd2407c785ed88c241a39699f257
Author: David Grudl <david@grudl.com>
Date:   Thu Jun 19 06:43:05 2008 +0000

    - simplified and improved Template
    - removed Template::$root, added Template::setCache()
    - new TemplateFilters: fragments, autoConfig
    - fixed Cache namespace in cleaning