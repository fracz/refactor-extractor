commit a288c0ef1c49d2fd1e03b4f4c20f6fb85a5d38e4
Author: mark_story <mark@mark-story.com>
Date:   Thu Jan 2 11:42:04 2014 -0500

    Add 'autoIncrement' support to MySQL.

    Add reflection and generation support for the autoIncrement property in
    MySQL. This helps improve composite primary key support as composite
    keys with one AUTO_INCREMENT column can now be created.