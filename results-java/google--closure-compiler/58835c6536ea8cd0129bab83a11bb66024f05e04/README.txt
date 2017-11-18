commit 58835c6536ea8cd0129bab83a11bb66024f05e04
Author: dimvar <dimvar@google.com>
Date:   Tue Jan 19 16:20:24 2016 -0800

    [NTI] Don't drop the typeEnv after analyzing the callee.
    Also, improve inference using the return type of loose functions.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=112526802