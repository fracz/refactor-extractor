commit 237bbd057869206a2ffba2c5da209ab672b79bb3
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Fri Dec 7 19:10:43 2012 +0100

    fixed and refactored YamlFileLoader in the same sense as the XmlFileLoader

    some nonsesense configs were not validated correctly like an imported resource with a pattern key. added tests for such things too.