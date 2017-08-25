commit 43adce6d5dd6ba6d26d98b6cc2c0183d750244c5
Author: ever.zet <ever.zet@gmail.com>
Date:   Thu Sep 16 14:25:59 2010 +0300

    refactoring & code review

     - removed @package from phpDoc (who need them, when we have
       namespaces?)
     - moved all logic from constructors to specific functions (for example
       loaders => load($paths))