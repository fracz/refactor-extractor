commit 13d9f8d53a6b28d431ce3769f65dfa9f9e5fc633
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Mon Sep 5 08:56:24 2016 +0700

    IDEA-CR-13504 Lambda to method-reference: PsiVariable is accepted as it's necessary for StreamApiMigrationInspection;
    StreamApiMigrationInspection.TerminalBlock simplified (myFrom/myTo did not actually improve performance, but added unnecessary logic)