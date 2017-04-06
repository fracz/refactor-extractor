commit feaee3615ff16e21b468ead9138214a34a5a9157
Merge: 7de4ec8 2850fca
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 6 18:50:21 2013 +0100

    Merge branch '2.2'

    * 2.2: (26 commits)
      [FrameworkBundle] Fixes invalid serialized objects in cache
      remove dead code in yaml component
      Fixed typo in UPGRADE-2.2
      fixed typo
      RedisProfilerStorage wrong db-number/index-number selected
      [DependencyInjection] added a test for the previous merge (refs #7261)
      Unset loading[$id] in ContainerBuilder on exception
      Default validation message translation fix.
      remove() should not use deprecated getParent() so it does not trigger deprecation internally
      adjust routing tests to not use prefix in addCollection
      add test for uniqueness of resources
      added tests for addDefaults, addRequirements, addOptions
      adjust RouteCollectionTest for the addCollection change and refactor the tests to only skip the part that really needs the config component
      added tests for remove() that wasnt covered yet and special route name
      refactor interator test that was still assuming a tree
      adjust tests to no use addPrefix with options
      adjusted tests to not use RouteCollection::getPrefix
      [Routing] trigger deprecation warning for deprecated features that will be removed in 2.3
      [Console] fixed StringInput binding
      [Console] added string input test
      ...