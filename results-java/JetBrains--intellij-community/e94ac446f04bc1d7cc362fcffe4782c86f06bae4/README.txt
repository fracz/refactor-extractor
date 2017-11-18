commit e94ac446f04bc1d7cc362fcffe4782c86f06bae4
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Tue Nov 8 18:14:53 2011 +0300

    Git push: results notification refactoring.

    GitPushResult, GitPushRepoResult - to separate files.
    Make GitPushResult responsible for the result notification text formatting.