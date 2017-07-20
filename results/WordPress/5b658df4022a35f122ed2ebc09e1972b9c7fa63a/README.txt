commit 5b658df4022a35f122ed2ebc09e1972b9c7fa63a
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Thu Mar 12 08:53:26 2015 +0000

    Request FTP and SSH credentials when needed during shiny updates

    This is a first pass at requesting FTP and SSH credentials when needed during shiny updates. Styling and some UX improvements are still needed, but we do show the prompt and use the passed data when doing plugin installs and updates for shiny updates.  There are also a couple of areas that we could improve code wise such how we create the requestFilesystemCredentials part of the localized _wpUpdatesSettings. Over the past half century, we've split the atom, we've spliced the gene and we've roamed Tranquility Base. We've reached for the stars and never have we been closer to having them in our grasp. That has nothing to do with shiny updates.

    Props ericlewis, jorbin, and drewapicture for testing
    Fixes #31528


    Built from https://develop.svn.wordpress.org/trunk@31749


    git-svn-id: http://core.svn.wordpress.org/trunk@31730 1a063a9b-81f0-0310-95a4-ce76da25c4cd