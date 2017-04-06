commit ddf6534cc684eb14fa634ff23a0108d3facea3d4
Merge: 694c74d 200ed54
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 19 19:46:30 2011 +0100

    merged branch stof/doctrine_choice_list (PR #2921)

    Commits
    -------

    200ed54 [DoctrineBridge] Extracted the common type and made the choice list generic
    3c81b62 [Doctrine] Cleanup and move loader into its own method
    7646a5b [Doctrine] Dont allow null in ORMQueryBuilderLoader
    988c2a5 Adjust check
    3b5c617 [DoctrineBridge] Remove large parts of the EntityChoiceList code that were completly unnecessary (code was unreachable).
    b919d92 [DoctrineBridge] Optimize fetching of entities to use WHERE IN and fix other inefficencies.
    517eebc [DoctrineBridge] Refactor entity choice list to be ORM independant using an EntityLoader interface.

    Discussion
    ----------

    [DoctrineBridge] Refactor EntityChoiceList

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no (99%)
    Symfony2 tests pass: yes

    This decouples the ORM from the EntityChoiceList and makes its much more flexible. Instead of having a "query_builder" to do smart or complex queries you can now create "loader" instances which can arbitrarily help loading entities.

    Additionally i removed lots of code that was unnecessary and not used by the current code.

    There is a slight BC break in that the EntityChoiceList class is now accepting an EntityLoaderInterface instead of a querybuilder. However that class was nested inside the EntityType and should not be widely used or overwritten.

    The abstract class DoctrineType is meant to be used as base class by other Doctrine project to share the logic by simply using a different type name and a different loader implementation.

    This PR replaces #2728.

    ---------------------------------------------------------------------------

    by beberlei at 2011/12/19 09:20:43 -0800

    Thanks for doing the last refactorings, this is now fine from my side as well.