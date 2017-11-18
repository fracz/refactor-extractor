commit 572ab92c59e55903e0daf406f9685e3779563c58
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Jul 8 11:01:03 2010 +0400

    IDEA-53596 Soft wrap for editors

    1. Defined correct logic for soft wraps processing on document content change;
    2. Moved offsets and positions mapping from SoftWrapModelImpl to SoftWrapDataMapper;
    3. Introduced TextChange interface and made TextChangeImpl mutable in order to avoid unnecessary construction of big number of soft wrap objects on document text change;
    4. Soft wraps processing refactorings;