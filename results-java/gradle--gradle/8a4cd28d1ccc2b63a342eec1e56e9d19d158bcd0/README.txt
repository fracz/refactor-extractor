commit 8a4cd28d1ccc2b63a342eec1e56e9d19d158bcd0
Author: Adam Murdoch <a@rubygrapefruit.net>
Date:   Wed Sep 2 21:07:07 2009 +0000

    Another quick performance improvement: Don't construct all the Ivy infrastructure when resolving a Configuration with no dependencies, just return an empty ResolvedConfiguration.

    git-svn-id: http://svn.codehaus.org/gradle/gradle-core/trunk@1817 004c2c75-fc45-0410-b1a2-da8352e2331b