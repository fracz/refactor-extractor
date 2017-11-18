commit 21353418dc594f93e6636e9f35e357e2d251ea45
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Tue Sep 14 11:48:15 2010 +0400

    IDEA-58070 Soft wrap: Improve soft wraps performance

    1. Fold regions processing logic is not activated in case soft wraps are disabled;
    2. Minor refactorings;
    3. Javadocs are added;