commit d107ad92a0d81c0f35579810256cd89d0ba96ee2
Author: Mikhail Mutcianko <mutcianko.m@gmail.com>
Date:   Mon Jul 21 12:37:36 2014 +0400

    refactor converter initialization

    - push lazyInit() down to IDE and Cli implementations
    - no file listeners created in cli implementation anymore
    - fix tests