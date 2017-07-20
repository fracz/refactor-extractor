commit 72065fb8351b54eee5f1928c18e06ad81aa9b502
Author: Dries Buytaert <dries@buytaert.net>
Date:   Wed Jun 20 20:00:40 2001 +0000

    - Added a brand-new access.module which allows you to manage 'roles'
      (groups) and 'permissions' ... (inspired by Zope's system).

        + Once installed, click the help-link for more information.

        + See updates/2.00-to-x.xx.sql for the SQL updates.

    - Modified loads of code to use our new access.module.  The system
      still has to mature though: new permissions have to be added and
      existing permissions need stream-lining.  Awaiting suggestions.

    - As a direct result of the new access system, I had to rewrite the
      way the top-level links in admin.php are rendered and displayed,
      and xhtml-ified admin.php while I was at it.

    TODO

    - Home-brewed modules need updating, home-brewed themes not.
      (Examples: file.module, trip_link.module)

    - As soon we *finished* the refactoring of the user system (KJ has
      been working on this refactoring already) we should consider to
      embed this role and permission code into account.module ...