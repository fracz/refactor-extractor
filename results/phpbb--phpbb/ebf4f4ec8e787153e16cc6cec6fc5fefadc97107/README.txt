commit ebf4f4ec8e787153e16cc6cec6fc5fefadc97107
Author: Nils Adermann <naderman@naderman.de>
Date:   Sun May 28 19:06:21 2006 +0000

    - added search by author_id to solve problems with looking up posts of users with a name containing wildcards
    - user based flood control (seperate limits for users and guests) [Bug #1357]
    - inform the user about ignored words if he receives a "no words specified" message
    - solve problems with the number of entries per page [Bug #1973]
    - different height for popup window ["Bug" #1814]
    - speed improvements for posting and search reindexing in fulltext_native
     -> use php files for ignore words and synonyms


    git-svn-id: file:///svn/phpbb/trunk@5981 89ea8834-ac86-4346-8a33-228a782c2dd0