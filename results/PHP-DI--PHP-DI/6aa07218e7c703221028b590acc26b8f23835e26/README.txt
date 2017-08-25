commit 6aa07218e7c703221028b590acc26b8f23835e26
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Sun Nov 29 16:20:52 2015 +0100

    Inject a new DI\Factory\RequestedEntry object instead of definitions into factories

    That allows to keep definitions an internal implementation details. If definitions are refactored later on, factories will not be broken. Also factories have no reason to know about definitions, they just want the request entry name (ISP).