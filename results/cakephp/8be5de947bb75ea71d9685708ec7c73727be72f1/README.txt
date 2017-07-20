commit 8be5de947bb75ea71d9685708ec7c73727be72f1
Author: mark_story <mark@mark-story.com>
Date:   Sun Aug 30 19:07:36 2009 -0400

    Minor refactor in how paths are checked, so that fallback extensions are checked only when all paths fail, instead of for each path. Refs #49