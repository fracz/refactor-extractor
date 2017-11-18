commit 8dea43190adeb2d399f780730c5055b25a9e8888
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Wed Oct 6 08:38:31 2010 +0400

    IDEA-59452 Exception during Diff view on git commit

    1. Soft wraps processing is correctly filtered by fold processing state (enabled/disabled);
    2. Minor refactorings;