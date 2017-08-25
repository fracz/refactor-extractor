commit ac21b7d47b4c8783a41ba4dbf8d26971c252c6d1
Author: Nils Adermann <naderman@naderman.de>
Date:   Sun Mar 4 16:05:17 2007 +0000

    - added a UNIQUE index on the wordmatch table
    - some modifications of search indexing which might improve the speed and hopefully fixes [Bug #8352]
    - added logging to search indexing [Bug #8384]


    git-svn-id: file:///svn/phpbb/trunk@7119 89ea8834-ac86-4346-8a33-228a782c2dd0