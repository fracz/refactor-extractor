commit f45937e17d6079d8743de6f7bafddee509bcc591
Author: Ryan Wyllie <ryan@moodle.com>
Date:   Fri Oct 23 07:19:50 2015 +0000

    MDL-51880 atto/plugins/table: improve table borders

    * Removed the "No borders" option from general borders setting
      since it's a style choice.
    * Added inherit, initial and unset as border style options.
    * Made the border settings disabled if "Theme default" is selected.
    * Changed the transparent colour to be "Theme default" which means
      apply no colour at all.