commit c1eb7e085b1ff0d8d3d8d4ac9836eff377bbb0f5
Author: johnlenz <johnlenz@google.com>
Date:   Sat Aug 20 00:29:51 2016 -0700

    FlowSensitiveInlineVariables should not inspect or modify externs.  Saves 500ms off a minimal compile in my tests of the js version the compiler.

    Since none of the functions in the externs have a body there are other general improvements that can be done (prototyped in cl/130822090) but those changes require more validation and this needs to be done regardless.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130826358