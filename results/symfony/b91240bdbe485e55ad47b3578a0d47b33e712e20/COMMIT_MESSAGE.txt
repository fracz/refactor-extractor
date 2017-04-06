commit b91240bdbe485e55ad47b3578a0d47b33e712e20
Merge: d162243 b228942
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Feb 1 20:35:44 2012 +0100

    merged branch Burgov/fix_entity_choice_list (PR #3234)

    Commits
    -------

    b228942 fix for entity choice list when ->loaded is false and the class name is an entity shorthand name and updated tests to work with refactored choicelist

    Discussion
    ----------

    fix for entity choice list when ->loaded is false and the class name is ...

    ...an entity shorthand name

    This bug was reintroduced after the latest choicelist refactoring and was originally fixed in 231e79ce0fa4bab295d8d347205fda0b6b468323

    ---------------------------------------------------------------------------

    by stof at 2012-02-01T15:17:37Z

    Please also add a unit test for this to avoid further regressions

    ---------------------------------------------------------------------------

    by stof at 2012-02-01T17:05:31Z

    btw, a better fix would be to put the real class name in ``$this->class`` to avoid doing it each time (you are doing it in a loop btw). We don't need to keep the short notation anyway

    ---------------------------------------------------------------------------

    by Burgov at 2012-02-01T17:19:01Z

    @stof done, thanks for the comments