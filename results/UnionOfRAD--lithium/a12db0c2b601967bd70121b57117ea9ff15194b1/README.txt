commit a12db0c2b601967bd70121b57117ea9ff15194b1
Author: Nate Abele <nate.abele@gmail.com>
Date:   Sat Mar 12 01:18:35 2011 -0500

    Making `\core\Libraries::instance()` filterable, and refactoring `\core\Object::_instance()` and `\core\StaticObject::_instance()` to depend on it. Updating affected test cases and console commands.