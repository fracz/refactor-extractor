commit 23c0f28705aefb00073b3d8c476e8386664d4e7b
Author: Anna.Kozlova <anna.kozlova@jetbrains.com>
Date:   Tue Jan 10 19:36:31 2017 +0100

    change signature: don't ask about covariant overrides when types are equal, e.g. when super type is changed based on override method return type
    type annotations: ensure annotations are cloned so invalidating initial type doesn't break the consequence refactoring
    EA-93296 - PIEAE: PsiUtilCore.ensureValid