commit 7b62937942d4506c2d2a23c4a7211adbeccd83d1
Author: marcus <marcus@36083f99-b078-4883-b0ff-0f9b5a30f544>
Date:   Mon Aug 25 14:45:49 2008 +0000

    Closes #254: If mb_string is installed then internationalised usernames are supported in file system.

    Because of this I have raised mbstring from recommended to a (non-fatal) core requirement.

    Because of the lack of a unicode ctype_alnum function the validation occurs at username input. Because of this I have improved the user registration code:

    This code now validates for special chars etc in the username. I have also introduced the following new plugin hooks (which are run after primary validation) which provide plugins with the ability to add other requirements (extra security etc).

    'registeruser:validate:password'
    'registeruser:validate:username'
    'registeruser:validate:email'

    Marcus Povey 25/8/08

    git-svn-id: https://code.elgg.org/elgg/trunk@2040 36083f99-b078-4883-b0ff-0f9b5a30f544