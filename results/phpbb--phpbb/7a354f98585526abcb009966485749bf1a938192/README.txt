commit 7a354f98585526abcb009966485749bf1a938192
Author: Tom Beddard <subblue@users.sourceforge.net>
Date:   Sat Dec 17 12:05:20 2005 +0000

    Tweaked so that css files included via @include file("file.css"); are brought into the main stylesheet.css before variable replacement. This greatly improves style organisation by enabling different stylesheets for the main sections of the forum


    git-svn-id: file:///svn/phpbb/trunk@5343 89ea8834-ac86-4346-8a33-228a782c2dd0