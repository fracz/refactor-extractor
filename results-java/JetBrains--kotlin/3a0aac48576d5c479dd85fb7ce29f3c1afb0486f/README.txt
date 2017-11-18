commit 3a0aac48576d5c479dd85fb7ce29f3c1afb0486f
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed May 7 14:15:29 2014 +0400

    Simplify type mapping logic in CodegenBinding

    - inline asmType to calling getAsmType, which does something more
    - refactor getJvmInternalName to use getAsmType as well
    - simplify getAsmType and fix a probable bug in mapping singletons nested in
      enums (which wasn't reproduced, though a test is added)
    - delete unnnecessary ASM_TYPE recording for enum entries in
      CodegenAnnotatingVisitor