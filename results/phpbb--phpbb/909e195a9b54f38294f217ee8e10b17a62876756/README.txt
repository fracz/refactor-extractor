commit 909e195a9b54f38294f217ee8e10b17a62876756
Author: Nils Adermann <naderman@naderman.de>
Date:   Sun Jul 15 20:53:27 2007 +0000

    - search result extract shouldn't end in the middle of a multibyte character [Bug #11863]
    - missing localisation for an imageset shouldn't create lots of "imageset refreshed" log messages [Bug #12027]
    - explain that themes which need parsing cannot be stored on the filesystem [Bug #11134]
    - normalize usernames (we really need to make sure we normalize everything)
    - improved utf8_clean_string, more complete list of homographs and NFKC normalization, also the resulting string is now trimmed
    - corrected searching subforums explanation [Bug #12209]


    git-svn-id: file:///svn/phpbb/trunk@7890 89ea8834-ac86-4346-8a33-228a782c2dd0