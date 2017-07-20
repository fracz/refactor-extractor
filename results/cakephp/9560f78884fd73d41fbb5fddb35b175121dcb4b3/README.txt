commit 9560f78884fd73d41fbb5fddb35b175121dcb4b3
Author: phpnut <phpnut@cakephp.org>
Date:   Tue Dec 27 03:33:44 2005 +0000

    Merging:

    Revision: [1638]
    removing php short tags

    Revision: [1637]
    Remove renderElememnts loading of helpers also, forgot it in the last commit

    Revision: [1636]
    Refactoring after profiling code.
    Session was creating a new instance of Dispatcher removed the need for it.
    Added a check to the Component class to pass the base to the SessionComponent class, will refactor that at a later time.

    Changed View class so it would not load helpers when rending a layout, no need for that.
    A great performance boost after the change.

    Change the loadModels method call in app/webroot/index.php.
    Will only attempt the loadModels call if the AppModel class is not in memory, and the Database class is in memory.
    Removed all unnecessary calls to basics uses(). Again another big performance increase.
    Added fix to the Html::guiListTree() after discussing the output that is expected.
    A ticket was closed on this already.

    Revision: [1635]
    Removing calls to basic uses()

    Revision: [1634]
    Removing calls to basics uses() that are not needed.

    Revision: [1633]
    Removing calls to basics uses() that are not needed.
    Moved Object class further up in the loading order

    Revision: [1632]
    adding fix for Ticket #132

    Revision: [1631]
    Added fix from Ticket #122

    Revision: [1630]
    Scaffold views can now be placed in a view directory.
    These will override the core.
    Example (Must have the scaffold dot name):
    app/views/posts/scaffold.list.thtml
    app/views/posts/scaffold.new.thtml
    app/views/posts/scaffold.edit.thtml
    app/views/posts/scaffold.show.thtml

    Revision: [1629]
    Think I fixed the issue with scaffold showing proper dates prior to January 1 1970 00:00:00.

    Revision: [1628]
    Added a few more change to allow saving dates prior to January 1 1970 00:00:00.
    Still a few issues with this, but will get them figured out soon.
    Changed scaffold to use only one form view.

    Revision: [1627]
    Added fix for Ticket #189

    Revision: [1626]
    Added fix for Ticket #120.

    Revision: [1625]
    left justified doc blocks

    Revision: [1624]
    remove files from uses() that are loaded by default in app/webroot/index.php no reason to attempt to load them again in the classes

    Revision: [1623]
    adding check to the loadModels and loadController that will only attempt to load files if the classes are not already in memory

    Revision: [1622]
    Adding fix to time helper that was lost in a previous merge
    Removing all tabs from code

    Revision: [1621]
    Addtional model validation fixes

    Revision: [1620]
    fixed parse error

    Revision: [1619]
    Fixing ticket #102

    Revision: [1618]
    correcting mime types and keywords

    Revision: [1617]
    correcting mime types and keywords

    Revision: [1616]
    fixed link in footer

    Revision: [1615]
    Fixing ticket #207

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@1639 3807eeeb-6ff5-0310-8944-8be069107fe0