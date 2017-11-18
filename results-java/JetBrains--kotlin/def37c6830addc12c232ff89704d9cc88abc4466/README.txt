commit def37c6830addc12c232ff89704d9cc88abc4466
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Aug 25 18:51:23 2014 +0400

    Test generator: improve import statements in generated tests

    - delete unused "junit.framework.Assert" import
    - delete import of the abstract super class, because it always happens to be in
      the same package
    - reorder other imports in such way that "Optimize Imports" action in IDEA will
      mostly have no effect in generated tests. However this will still happen in
      tests without any nested test cases (useless imports of InnerClasses etc.)