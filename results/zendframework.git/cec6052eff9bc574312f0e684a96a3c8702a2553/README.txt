commit cec6052eff9bc574312f0e684a96a3c8702a2553
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu Jul 12 15:45:45 2012 -0500

    [zen-78] Removal of Wildfire component

    - This was using the ZF1 MVC, and needed to be heavily refactored to work with
      ZF2. At this time, we feel it makes more sense to simply use the official
      FirePHP library and write a logger consuming it.