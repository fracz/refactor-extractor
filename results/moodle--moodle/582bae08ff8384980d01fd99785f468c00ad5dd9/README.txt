commit 582bae08ff8384980d01fd99785f468c00ad5dd9
Author: Petr Skoda <skodak@moodle.org>
Date:   Sun Sep 19 13:09:48 2010 +0000

    MDL-24148 several course delete improvements and fixes:
     * fixed order of deleting in course adn context
     * adding course context to event data because it is not available alter and some stuff may depend on old context id
     * adding option for context purging (keeps the context record because it might be still referenced later and it would be recreated)
     * new course enrol cleanup
     * removing content from some course fields that were referencing deleted content
     * coding style and phpdocs improvements