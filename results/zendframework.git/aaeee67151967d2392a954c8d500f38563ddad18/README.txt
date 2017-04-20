commit aaeee67151967d2392a954c8d500f38563ddad18
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu Jul 12 15:49:12 2012 -0500

    [zen-78] Removal of Test component

    - Many of the assertion types in here are now available in PHPUnit, albeit with
      different syntax. Additionally, the main thrust of this was for testing ZF1
      applications, and as such, has little relevance to ZF2 (other than the query
      selectors, which are in PHPUnit). Finally, the DB component has not been
      refactored for ZF2, and we may want to approach this differently anyways.