commit d4b18ffb78ae69ac5b14792e9f3749f3b11df68f
Author: Jonathan Stanley <shs@users.sourceforge.net>
Date:   Mon Apr 9 02:59:26 2007 +0000

    Blargh! Continuation of bug #9546 and some initial RTL related stuff. All prosilver images now have dimensions defined and CSS tidied up and refactored such that all localised images will show with their proper dimensions (eg: English imageset, "Locked Topic" and "New PM" had "phantom" white space to their right as clickable area)... which means languages like Turkish and Chinese, which typically have much short language strings can now have more "compact" images... and whatever languages that need 15+ characters for the equivalent of "Post" can now do so without having to shoehorn into a 96px width "big button".


    git-svn-id: file:///svn/phpbb/trunk@7306 89ea8834-ac86-4346-8a33-228a782c2dd0