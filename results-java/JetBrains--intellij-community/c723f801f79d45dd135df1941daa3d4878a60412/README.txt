commit c723f801f79d45dd135df1941daa3d4878a60412
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Jul 8 14:29:54 2010 +0400

    IDEA-53596 Soft wrap for editors

    1. Corrected 'soft wrap' -> 'hard wrap' processing;
    2. Profiled soft wraps processing and performed couple of optimizations within that;
    3. Minor refactorings;