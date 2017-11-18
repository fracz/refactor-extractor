commit fd04ce89d70e8897162e68e41a892eff91e3f9d5
Author: nik <Nikolay.Chashnikov@jetbrains.com>
Date:   Fri Jun 2 20:37:45 2017 +0200

    refactoring: ModuleGroupingTreeHelper abstracted over type of module instances

    This is needed to reuse the helper for trees consisting of ModuleDescription nodes.