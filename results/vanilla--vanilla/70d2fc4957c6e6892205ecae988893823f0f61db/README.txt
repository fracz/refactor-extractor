commit 70d2fc4957c6e6892205ecae988893823f0f61db
Author: Todd Burry <todd@vanillaforums.com>
Date:   Mon Dec 7 12:53:10 2015 -0500

    Add Gdn_Model->deleteID().

    A lot of models override Gdn_Model->delete() to mean deleting by a single ID so this method has been added so that models can be refactored to match the class template.