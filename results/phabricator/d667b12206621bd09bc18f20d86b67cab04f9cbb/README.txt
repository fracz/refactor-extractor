commit d667b12206621bd09bc18f20d86b67cab04f9cbb
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 19 11:05:17 2013 -0800

    Provide a standalone query for resolution of commit author/committer into Phabricator users

    Summary:
    Ref T4195. To implement the "Author" and "Committer" rules, I need to resolve author/committer strings into Phabricator users.

    The code to do this is currently buried in the daemons. Extract it into a standalone query.

    I also added `bin/repository lookup-users <commit>` to test this query, both to improve confidence I'm getting this right and to provide a diagnostic command for users, since there's occasionally some confusion over how author/committer strings resolve into valid users.

    Test Plan:
    I tested this using `bin/repository lookup-users` and `reparse.php --message` on Git, Mercurial and SVN commits. Here's the `lookup-users` output:

      >>> orbital ~/devtools/phabricator $ ./bin/repository lookup-users rINIS3
      Examining commit rINIS3...
      Raw author string: epriestley
      Phabricator user: epriestley (Evan Priestley   )
      Raw committer string: null
      >>> orbital ~/devtools/phabricator $ ./bin/repository lookup-users rPOEMS165b6c54f487c8
      Examining commit rPOEMS165b6c54f487...
      Raw author string: epriestley <git@epriestley.com>
      Phabricator user: epriestley (Evan Priestley   )
      Raw committer string: epriestley <git@epriestley.com>
      Phabricator user: epriestley (Evan Priestley   )
      >>> orbital ~/devtools/phabricator $ ./bin/repository lookup-users rINIH6d24c1aee7741e
      Examining commit rINIH6d24c1aee774...
      Raw author string: epriestley <hg@yghe.net>
      Phabricator user: epriestley (Evan Priestley   )
      Raw committer string: null
      >>> orbital ~/devtools/phabricator $

    The `reparse.php` output was similar, and all VCSes resolved authors correctly.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1731, T4195

    Differential Revision: https://secure.phabricator.com/D7801