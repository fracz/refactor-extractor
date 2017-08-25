commit 650462dfacf78522166996233a1b60a772c94689
Author: Petr Skoda <skodak@moodle.org>
Date:   Wed Sep 15 15:55:18 2010 +0000

    MDL-24211 events subsystem cleanup and improvements
    * handlers can be defined as internal or external
    * external handlers are not called instantly when inside DB transaction
    * code cleanup
    * more robust detection of broken handlers
    * refixed unit tests
    * removing all pending events from 1.9 - these can not be processed due to encoding changes
    * finally using real 'component' in handlers table