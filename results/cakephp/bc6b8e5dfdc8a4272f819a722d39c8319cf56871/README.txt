commit bc6b8e5dfdc8a4272f819a722d39c8319cf56871
Author: Mark Story <mark@mark-story.com>
Date:   Fri Apr 23 00:04:15 2010 -0400

    Fixing parameters from leaking into the script tag when calling JsHelper::submit().  Added test cases and refactored JsHelper::link().  Fixes #613