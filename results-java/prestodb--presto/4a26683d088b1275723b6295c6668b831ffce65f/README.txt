commit 4a26683d088b1275723b6295c6668b831ffce65f
Author: Andrzej Fiedukowicz <Andrzej.Fiedukowicz@teradata.com>
Date:   Mon May 16 20:03:02 2016 +0200

    Refactor Signature to take TypeSignature instead of String

    This is second step of refactoring focusing on avoiding use of
    combination of String and LiteralParameters after first function
    processing.

    The main advantages of this changes are:
     - Use of semanticaly descriptive structure of TypeSignature instead of general type String.
     - Avoidance of processing String and LiteralParameters in code that does only need TypeSignature.
     - Creation of TypeSignature once instead of creating it on demand from string
       (with LiteralParameters required to proceed).
     - Moving away from Strings ASAP!