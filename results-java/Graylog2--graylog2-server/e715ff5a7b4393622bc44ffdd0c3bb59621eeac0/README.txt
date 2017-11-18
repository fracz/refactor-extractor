commit e715ff5a7b4393622bc44ffdd0c3bb59621eeac0
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Tue Jun 24 15:02:02 2014 +0200

    refactor loading drools rules

    rules or rules files that fail to compile are ignored and don't cause startup failures anymore

    fixes #587
    issue #499