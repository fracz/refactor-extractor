commit bfa4e6038e1e15bab8c19a6a0caa9607933b972b
Author: Meik Sievertsen <acydburn@phpbb.com>
Date:   Sat Feb 28 19:20:29 2009 +0000

    refactor the database updater:
    - no longer support versions prior 3.0.0
    - more apparent place for adding schema/data changes
    - use db_tools
    - added check for wrong schema (MySQL 3x/4x schema on MySQL > 4.0)


    git-svn-id: file:///svn/phpbb/branches/phpBB-3_0_0@9350 89ea8834-ac86-4346-8a33-228a782c2dd0