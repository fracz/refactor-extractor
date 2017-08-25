commit eb3884e48f40e05fdf5a7caa01fdfda4934e94ee
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Tue Nov 24 15:58:54 2015 +0000

    MDL-52292 block testing generators: improve the API

    * Refactor the block generator base class, to remove the amount
      of duplication required in base classes.

    * Change the defaults that are filled in to be a little more natural.

    * Make the Behat step 'Given the following "block" exist:' work.