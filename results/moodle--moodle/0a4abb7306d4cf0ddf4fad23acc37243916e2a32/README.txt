commit 0a4abb7306d4cf0ddf4fad23acc37243916e2a32
Author: Sam Hemelryk <sam@moodle.com>
Date:   Wed Dec 16 02:00:48 2009 +0000

    mod-lesson MDL-21006 Huge refactoring of the lesson code
    The following are notable changes made in this commit
    * Lesson page type are now class based and extend an abstract class. This includes a class for the page type and a class for the creating/editing a instance of this page.
    * Converted all forms to mforms
    * Action script located in mod/action/* were worked into the above so far less switch statements and the action directory will be removed.
    * Implements a custom renderer
    * Converted everything to use page, output, and custom renderer methods
    * Replaced all deprecated methods incl. print_textarea conversions
    * Tried to cut down on excessive DB calls.
    Things worth noting:
    * The focus of this patch was on cleaning up the module not rewriting it, as such I have organized NOT rewritten. There are still many areas in the module where the code could be greatly improved however to do so would require a rethink/rewrite