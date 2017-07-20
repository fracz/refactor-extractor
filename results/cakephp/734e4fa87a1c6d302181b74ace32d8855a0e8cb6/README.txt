commit 734e4fa87a1c6d302181b74ace32d8855a0e8cb6
Author: Mark Story <mark@mark-story.com>
Date:   Thu Apr 29 21:40:12 2010 -0400

    Removing the conditional check around including custom session configuration files.  This fixes issues where requestAction could cause loss of session settings, and improves the end developers ability to customize the session.  Custom session files should ensure that classes/functions are conditionally declared.  Fixes #374, #541