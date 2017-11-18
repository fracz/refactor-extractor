commit e14184ce12ec2a373541c666e6015f2689d8f83a
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 14 19:56:13 2011 +0100

    Tooling api improvements. Connection object is now thread safe.

    Sort of a mean check-in but it was hard to chop it into steps. Details:

    -The fixes were applied in the consumer code which means the fixes should apply to previous target gradle versions. Alternative approach to the problem was changing the provider code and attach logging tools to the InternalLongRunningOperationParameters so that they are created per operation.
    -Started using ThreadLocal for the progressLoggerFactory and listenerManager. Those two are paired together (they share some state). Added a connection decorator that initializes the ThreadLocal state before other connection implementations kick in.
    -Distribution and ToolingImplementationLoader no longer keep ProgressLoggerFactory. It is passed as method argument. This makes many things much simpler as we don't have to worry about that state in concurrent conditions
    -Added/improved corresponding coverage