commit ce8b00801edabd8de8b88891d6997309e7ca0459
Author: Nils Adermann <naderman@naderman.de>
Date:   Tue Mar 13 22:00:55 2007 +0000

    - improvements to search indexing performance, espacially tidy() by adding a word_count column, the database update from b5 to next version will take quite a while on bigger databases, I also lowered the default common word threshold from 20 to 5 percent, big boards might want to use 3 or 2 percent, 20 was way too high
    - added some keys to ACL tables, great improvement of auth query performance
    - we will only add new language strings to install.php language file and won't modify any, if a language file is updated before phpBB is updated, the updater will not overwrite the user's language with english if install.php was modified


    git-svn-id: file:///svn/phpbb/trunk@7182 89ea8834-ac86-4346-8a33-228a782c2dd0