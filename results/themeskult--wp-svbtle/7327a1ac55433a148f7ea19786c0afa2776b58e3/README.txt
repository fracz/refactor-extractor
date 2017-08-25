commit 7327a1ac55433a148f7ea19786c0afa2776b58e3
Author: andrewsfreeman <andrew.s.freeman@gmail.com>
Date:   Sat Jul 28 19:17:48 2012 -0700

    Fixes #30. Adds a conditional which checks if idea_title is set, and if not, defaults to post_title. Necessary due to (awesome) refactoring of process_post and the creation of a new post when an idea is submitted...