commit 57648c5492806a351b2af0e5e13852063b39be51
Author: Mark Story <mark@mark-story.com>
Date:   Fri Apr 23 00:04:15 2010 -0400

    Fixing parameters from leaking into the script tag when calling JsHelper::submit().  Added test cases and refactored JsHelper::link().  Fixes #613