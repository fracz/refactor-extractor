commit 39d6cedaf5a7e52fe54d7d73ea22ec07cd5c2fb1
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Thu Apr 20 14:29:11 2017 +0200

    json schema refactoring:
    - brush up JsonSchemaServiceImpl, inner state as inner class to keep synchronization and updates tight, service is a client of state now
    - get rid of ordering schema providers, now resolution of references does not replay on the order of load
    - return back lost binding to javascript configs schema provider factory