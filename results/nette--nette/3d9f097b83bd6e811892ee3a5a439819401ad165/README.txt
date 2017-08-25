commit 3d9f097b83bd6e811892ee3a5a439819401ad165
Author: David Grudl <david@grudl.com>
Date:   Fri Aug 8 10:46:16 2008 +0000

    - removed ICausedException (exception chaining is implemented in PHP 5.3)
    - fixed Debug & ErrorException invalid stack trace workaround
    - improved Object's extension methods handling