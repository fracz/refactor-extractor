commit 011e6e9cee0577b702726b692a8699a445f848c5
Author: srowen <srowen@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Thu Jun 26 19:49:38 2008 +0000

    Big refactoring of ParsedResult: now split into ResultParser and ParsedResult classes, per Christian's suggestion. This unifies the parsed results that are produced from various input, simplifying client handling of different types.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@482 59b500cc-1b3d-0410-9834-0bbf25fbcc57