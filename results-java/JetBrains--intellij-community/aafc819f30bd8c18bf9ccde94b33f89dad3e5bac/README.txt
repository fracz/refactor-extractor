commit aafc819f30bd8c18bf9ccde94b33f89dad3e5bac
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Wed Jul 7 13:37:18 2010 +0400

    IDEA-53596 Soft wrap for editors

    1. Fixed (x; y) -> VisualPosition mapping in case of soft wrap-introduced virtual space;
    2. Fixed 'smart end' processing in case of soft wrapped lines before the current visual line;
    3. Minor refactoring;