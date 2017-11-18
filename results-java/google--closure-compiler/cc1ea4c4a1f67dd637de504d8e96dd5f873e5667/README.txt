commit cc1ea4c4a1f67dd637de504d8e96dd5f873e5667
Author: dimvar <dimvar@google.com>
Date:   Thu Aug 18 14:29:18 2016 -0700

    [NTI] Massive refactoring to remove static fields from newtypes classes, move them to JSTypes, and pass around the JSTypes instance as needed.

    This makes NTI thread safe. In future CLs, I will add info related to the compatibility mode to JSTypes.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130681919