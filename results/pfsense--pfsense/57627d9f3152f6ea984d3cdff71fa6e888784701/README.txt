commit 57627d9f3152f6ea984d3cdff71fa6e888784701
Author: Renato Botelho <garga@FreeBSD.org>
Date:   Thu Jun 19 12:23:44 2014 -0300

    Fix status_rrd_graph_img.php and also improve it:

    - Remove escapeshellarg that broke command line
    - Only remove dangerous chars to avoid command injection
    - Replace all `hostname` calls by php_uname('n')
    - Replace all `date` calls by strftime()
    - Add $_gb to collect possibly garbage from exec return