commit 710c84c566db441cb45b93983487a75df5a7c245
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Aug 17 15:49:52 2015 +0300

    Half-done huge refactoring aimed to unify support of various types of docstrings

    * Introduce new entities: DocStringBuilder, DocStringProvider and DocStringUpdater
    * StructuredDocString serves only as dumb parser and operates only on
    the level of Substrings to preserve offsets