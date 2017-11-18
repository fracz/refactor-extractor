commit 5b1bf92a49427947996f5ce2bd0467cd63b18312
Author: szczepiq <szczepiq@gmail.com>
Date:   Sat Apr 2 18:14:59 2011 +0200

    This is beautiful... (still refactoring the GeneratorTask for the sake of tooling api). I managed to refactor ideaModule task so that it:
    -is configured at 'configuration time'
    -does not require specific 'ideaConfigurer' task at all (this task is gone)
    -is still backwards compatible (but unfortunately it requires some extra hacky code)