commit 6b04fdc0b6abac8ca0118671cba48d9c718b6b5b
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Apr 15 14:07:21 2012 +0200

    MDL-32400 improve module generators

    Module generators are using standard *_add_instance() methods which helps with testing, it also updates grades and calendar events if used.