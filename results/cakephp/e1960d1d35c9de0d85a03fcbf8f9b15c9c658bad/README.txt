commit e1960d1d35c9de0d85a03fcbf8f9b15c9c658bad
Author: mark_story <mark@freshbooks.com>
Date:   Thu Jul 28 22:17:12 2011 -0400

    Changing Postgresql to use DELETE FROM instead of TRUNCATE.
    This should improve compatibilty with databases using constraints.
    Fixes #1838