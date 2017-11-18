commit 2b4ae9fe74d99268fbcc0721a240afa940c56644
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Oct 9 13:44:06 2008 +0000

    another step to refactor verifiers, moved some classes around

    --HG--
    rename : src/org/mockito/internal/progress/VerificationMode.java => src/org/mockito/internal/verification/VerificationMode.java
    rename : src/org/mockito/internal/progress/VerificationModeImpl.java => src/org/mockito/internal/verification/VerificationModeImpl.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40930