commit cc7ed2ba549424dc5c91bc9450b3ec9b8b47b590
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Jul 4 15:45:16 2017 +0300

    Change map key type in AnnotationDescriptor.getAllValueArguments

    Turns out, only the parameter's name is needed at all usages of this
    method. Such a map is both easier to use (no need to call
    ValueParameterDescriptor.getName) and easier to construct (no need to
    resolve annotation class, its constructor, its parameters). In this
    commit, only usages have changed but the implementations are still using
    the old logic, this is going to be refactored in subsequent commits