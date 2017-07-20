commit 9b2c0a7a450fff3b634f8119c8003ae1d20b97d0
Author: Fabian Becker <fabian.becker@uni-tuebingen.de>
Date:   Thu Jul 18 11:33:23 2013 +0200

    Refactor class Piwik_Commin to \Piwik\Core\Common

    Notice that auto refactoring has created a nested namespace. Not sure this is what we want - so we might have to edit those nested namespaces afterwards (I think they don't look so good)