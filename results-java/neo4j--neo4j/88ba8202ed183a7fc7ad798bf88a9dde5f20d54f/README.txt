commit 88ba8202ed183a7fc7ad798bf88a9dde5f20d54f
Author: Stefan Plantikow <stefan.plantikow@googlemail.com>
Date:   Fri Feb 5 15:47:56 2016 +0100

    Introduce StandardInternalExecutionResult as a refactoring of AcceptingInternalExecutionResult

    o Move procedure eagerization to ProcedureMode.call
    o Extracting scala <-> java value conversion and result value text support for future refactoring