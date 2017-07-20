commit 03b4953f008c1063e6d7166143ba844e8c6e89cc
Author: Fabian Becker <fabian.becker@uni-tuebingen.de>
Date:   Thu Jul 18 11:45:02 2013 +0200

    Refactor class Piwik_Common to \Piwik\Core\Common

    Notice that auto refactoring has created a nested namespace. Not sure this is what we want - so we might have to edit those nested namespaces afterwards (I think they don't look so good)