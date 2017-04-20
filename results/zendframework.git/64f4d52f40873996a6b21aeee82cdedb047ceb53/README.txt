commit 64f4d52f40873996a6b21aeee82cdedb047ceb53
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu Dec 15 17:36:16 2011 -0600

    Continued refactoring of Cache consumers

    - Mostly testing
    - Discovered some anomalies in PhpSerializer
    - Discovered some issues with reference passing in Filesystem cache adapter
    - Something is wrong with how the CLDR is using caching, which is
      affecting all Locale-based classes and their tests.
    - Tags are NOT WORKING in the Filesystem adapter; cannot find items by
      tags. This is affecting the Paginator tests in particular, and
      potentially may be the problem with the CLDR usage as well.