commit e38edbe05a5ff0b80ecdea30a59c28e65c7999e9
Author: Luke Daley <ld@ldaley.com>
Date:   Wed Feb 5 15:41:37 2014 +1000

    Remove implicit defaults from ScriptPluginFactory.

    This complicates some clients of ScriptPluginFactory, but makes its usage more consistent and comprehensible. Futher refactorings are to follow.