commit 1ef4188f1a8f315bfe011c7c2995c5171133e9cd
Author: Ekaterina Shliakhovetskaja <Ekaterina.Shliakhovetskaja@jetbrains.com>
Date:   Sun May 10 16:10:49 2009 +0400

    SpellChecker plugin: refactoring, add StringWithMistakesInspection, improve tests (extend JavaCodeInsightFixtureTestCase), bug fixes: (exclude from spellchecking tags author and link, use PsiNameHelper to avoid keywords spellchecking, avoid double check in jsp and js files, improve rules for splitting words in complex cases)