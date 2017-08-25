commit a368a026db2e79ef7c48f5db0fbff27da5468c26
Author: Ermal Luçi <eri@pfsense.org>
Date:   Tue Jun 30 17:11:30 2009 +0000

    * Reorganize the 'apply' button infrustructure in the GUI.
            - Present three new functions is/mark/clear_subsystem_dirty('name_of_subsystem'). This makes easier to create such things without needing to introduce new globals.
            - Convert all pages to the new infrustructure
            - This improves a lot the control on this notification