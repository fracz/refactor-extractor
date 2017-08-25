commit f78c14a8aeff8f8bc09540f263f4264de480da39
Author: Andy Fragen <andy@thefragens.com>
Date:   Mon Dec 9 21:46:31 2013 -0800

    first create stdClass with all plugin values, hopefully this will lead to a better refactoring.

    Big issue is that the actual updating doesn't seem to work. `upgrader_source_selection` filter doesn't seem to be firing. This commit has Firebug functions all over.