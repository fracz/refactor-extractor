commit 9e24fabf1232f129e5aac45802cce6dfd5e300ce
Author: Rich Hickey <richhickey@gmail.com>
Date:   Sat Apr 19 16:52:33 2008 +0000

    refactor to remove static init cycle between RT and Compiler, in order to run RT.init in static init