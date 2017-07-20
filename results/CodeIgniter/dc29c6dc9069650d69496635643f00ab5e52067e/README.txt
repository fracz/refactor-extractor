commit dc29c6dc9069650d69496635643f00ab5e52067e
Author: w0den <w0den@live.com>
Date:   Mon May 11 18:58:20 2015 +0300

    Improve Cache Query String behaviour

    Typically, in most cases, we do not need to cache all the Query String variables. That's why I suggest to improve `Cache Include Query String` behaviour — allow the developer to independently specify which variables should be cached.

    For example, consider a query to the following URL address:
    http://site.com/search?q=query&page=2&session=abcd&utm_source=web

    In this case we don't need to build md5 hash for entire query string, because `session` or `utm_source` can be different for all users. The only variables which should be used for md5 hash should be `q` and `page`. Thus, in `config.php` we can use `$config['cache_query_string'] = array('page', 'q');`.

    So:
    `$config['cache_query_string'] = FALSE;` → Cache Include Query String is disabled
    `$config['cache_query_string'] = TRUE;` → Cache Include Query String is enabled for all
    `$config['cache_query_string'] = array('page', 'q');` → enabled only for specified variables