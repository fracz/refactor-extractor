commit f7465bfcf331517f366153f612f613bf18a40c3f
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Wed Jun 29 20:50:27 2016 +0000

    Comments: Further improve text of initial comment.

    In [37888], the comment text was initially updated, however an opportunity to make the text less trivializing of actions that users will take was missed. To quote Helen Hou-Sandi: "It's important not to trivialize actions that are perfectly reasonable for a user to find tricky (where to go to log in is not exactly intuitive, even with the rewrites we have now)."

    The text of the comment now reads:

    > Hi, this is a comment.
    > To get started with moderating, editing, and deleting comments, please visit the Comments screen in the dashboard.
    > Commenter avatars come from <a href="https://gravatar.com">Gravatar</a>.

    Some interesting reading on the topic of "Simple" and "Easy" and how they relate to software development can be found at http://andrewspittle.com/2012/01/31/avoiding-easy/ and written by andrewspittle.

    Props helen.
    Fixes #36702.


    Built from https://develop.svn.wordpress.org/trunk@37921


    git-svn-id: http://core.svn.wordpress.org/trunk@37862 1a063a9b-81f0-0310-95a4-ce76da25c4cd