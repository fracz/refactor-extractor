commit 443a702376a4a49ba8aea9ec1ab8f07090c20d35
Author: pies <pies@3807eeeb-6ff5-0310-8944-8be069107fe0>
Date:   Sun Jun 5 11:05:24 2005 +0000

    I've merged in Olle's changes.

    Larry, I've merged in _some_ of your changes, I'll merge in the scaffolding and joins code when you tell me it's ready. But I don't want to break how the Controller class works, can't we really do without the constructClasses() method call? Which reminds me, with your joins code, will we be able to use constructs like $user->post->findAll() and $user->post->save()?

    Also, what are your changes to the DBO_MySQL class? I mean the mysqlResultSet(), and fetchResult() methods. I didn't see any MySQL-specific code inside them, perhaps they belong to the DBO class itself?

    - I've changed the headers on user-editable files in /app and /config. I hope they will constitute a compromise between readability and legality. I've left file Id, copyright, and licence notices.

    - /libs/basic.php::uses() function logs included files in global $loaded. Please, consider it a note to myself. Also, I've moved the NeatArray class out of the /libs/basics.php (into /libs/neat_array.php).
    - Some cleanups in the Controller and Dispatcher classes.
    - DBO::Prepare() accepts strings _and_ arrays now. It's a step towards a unified params theory.
    - I think I've added some comments to DBO sub-classes, but it might have been Olle.
    - A fix in Model class (findAll didn't work properly)
    - Object's constructor sets $this->db to &DBO, which means all Object-descendand classes have default access to the database if it's connected. We need to clean up the code accordingly (some classes set their own $this->db references).

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@236 3807eeeb-6ff5-0310-8944-8be069107fe0