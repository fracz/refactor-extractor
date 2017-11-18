commit eb3aac9acb24dcf65da432414a9f9c615fec585f
Author: James Strachan <james.strachan@gmail.com>
Date:   Sat Mar 3 12:11:06 2012 +0000

    refactored std.* package to be kotlin.*. Due to KT-1381 I had to move the functions from kotlin.test into the stdlib for now (I made them not depend on JUnit for now)