commit 2df9d2067df329db2737439d03c72203981f029f
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Nov 1 13:32:40 2011 -0500

    Complete refactoring of XML-RPC to use new HTTP API

    - Found errors in header management (was resetting headers instead of adding to
      them)
    - Found test issues and corrected