commit 6e230a4ab7f5f108d9e2ddea74b16e814d44d0d8
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Mon Aug 12 17:03:42 2013 +0400

    IDEA-111651 External System: after opening of an external project tasks of sub-projects do not appear in External system tool window until project refresh
    1. Don't drop local external settings info for external sub-projects;
    2. Tests refactoring;