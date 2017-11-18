commit e63a087ee560c69528d9d328305f84d8b35319b1
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Jul 15 19:22:02 2013 +0400

    Minor refactoring in codegen

    Replace String by JvmClassName where possible in OwnerKind, ClassFileFactory,
    NamespaceCodegen, add NotNull annotations, some minor renames, etc.