commit adf2b0a062f4e28ee5a895b3f35d65940c282cc3
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Wed Jul 7 11:57:17 2010 +0400

    IDEA-53596 Soft wrap for editors

    1. Encapsulated functionality of soft wrap appliance and construction. Generic algorithm that is expected to fit all supported languages/file types is applied. Target method is documented to be defined as extension point if general algorithm is not ok;
    2. Positions & offsets translation algorithm is corrected in order to respect 'after soft wrap' drawing-introduced width;
    3. Minor refactorings, javadocs;