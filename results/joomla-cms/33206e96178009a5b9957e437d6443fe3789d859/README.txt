commit 33206e96178009a5b9957e437d6443fe3789d859
Author: Hannes Papenberg <info@joomlager.de>
Date:   Tue Jan 12 07:56:18 2010 +0000

    ^ changed JLanguage to use parse_ini_file() to handle translations - big performance improvement!
    ^ changed all language files to adhere to the standards required by parse_ini_file() - the files still need to be changed to the new language strings!
    ^ changed language strings in the code to adhere to the strings required by parse_ini_file()
    # fixed XML file of mod_banners

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@14119 6f6e1ebd-4c2b-0410-823f-f34bde69bce9