commit 677f9aedbf78cf4acc3f4f26d8b48afd11590584
Author: David M <davidmj@users.sourceforge.net>
Date:   Thu Feb 9 07:02:50 2006 +0000

    - It turns out that a very fast Adler-32 implementation exists, this removes the bulk of my runtime fears and allows me to make the script a little more readable.
    - Found a neat way to express the size of the image computationally instead of counting the length
    - Removed pointless iteration variables, they removed readability


    git-svn-id: file:///svn/phpbb/trunk@5538 89ea8834-ac86-4346-8a33-228a782c2dd0