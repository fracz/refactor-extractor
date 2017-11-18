commit 7e599bca8f99f4f981d7f55406d26f28b180eea8
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Fri Jun 14 16:57:04 2013 +0400

    IDEA-108974 External system: Provide ability to link external project via 'external system' tool window

    1. 'Import from external system' now doesn't allow adding external projects already linked to the current ide project;
    2. External system tool window now has 'attach external project' button which delegates to the standard ide 'import module' facility;
    3. Minor refactoring;