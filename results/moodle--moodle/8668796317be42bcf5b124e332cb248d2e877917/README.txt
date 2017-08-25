commit 8668796317be42bcf5b124e332cb248d2e877917
Author: Damyon Wiese <damyon@moodle.com>
Date:   Wed May 4 16:45:33 2016 +0800

    MDL-54046 enrol: Make docs urls match the old path

    Previously each enrolment method had it's own url in the docs. Because of the
    refactor in 3.1 all pages are now served from the same script. We need to
    manually set the docs url so that it points to the old docs pages.