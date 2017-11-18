commit 2518ba33539ab782b0c5fd5c5f70bff7f337819b
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Mar 15 21:24:41 2013 +0400

    Minor refactoring of TImpl codegen

    jvmSignature.getAsmMethod() and functionOriginal are supposed to be the same
    entity, so move jvmSignature out of the way to where it's needed