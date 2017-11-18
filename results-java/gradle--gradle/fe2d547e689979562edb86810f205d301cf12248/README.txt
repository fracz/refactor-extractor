commit fe2d547e689979562edb86810f205d301cf12248
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Aug 9 23:53:03 2011 +0200

    GRADLE-1736 Idea/eclipse plugin fails fast when some dependency is unresolved. Also affects the Tooling API (cannot build model when any dependency is unresolved). Details:
     -Introduced LenientConfiguration that provides dependencies even if there are unresolved dependencies. ResolvedConfiguration is not very usable in that respect because it fails eagerly when at least one dependency is unresolved. The LenientConfiguration naming & api is an attempt to fix the problem in incremental fashion. In the long run, we should change the ResolvedConfiguration so that it returns all dependencies, be it resolved or unresolved. However, that's much bigger change & needs more effort & design.
     -LenientConfiguration acts as regular ResolvedConfiguration if all dependencies are resolved. This means little impact because ivy hackiness only kicks in when we have unresolved dependencies (and that situation usually means that you can do very little with gradle build).
     -Only IDE plugins utilize the lenient configuration at the moment.
     -LenientConfiguration can be queried for UnresolvedDependencies that contain the exception (with stack trace!) that caused the resolution failure. This should be very useful to fix GRADLE-1720 (lack of stack trace when ivy resolution fails for interesting reasons)
     -The fix should also help with eclipse & tooling api however I don't have yet automated coverage for it.
     -I'm not very happy with the implementation / api modification but it was best I could think of considering compatibility, incremental approach & low impact (the dependency resolution is quite important so let's not f** it up just now...)
     Pending:
     -code review
     -potential refactoring of the LenientConfiguration towards new api (DependencySet, etc.)
     -automated coverage for tooling api & eclipse
     One more sentence so that I'll beat the Guinness in the longest git comment. And now I won.