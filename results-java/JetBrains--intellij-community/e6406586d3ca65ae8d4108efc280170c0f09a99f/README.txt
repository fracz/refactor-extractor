commit e6406586d3ca65ae8d4108efc280170c0f09a99f
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Jul 1 16:09:15 2010 +0400

    IDEA-53596 Soft wrap for editors

    1. Introduced pure mock unit testing framework for soft wraps offsets & positions transformations;
    2. Added unit tests for soft wrap processing;
    3. Soft wrap processing is refactored in order to improve maintainability (as it's possible to check if the change brokes processing now);
    4. Corrected left and right arrows processing in case of soft wraps (move to the next/previous visual soft-wrapped line);