commit 8d103a08984ac2de72d756eb5c5c8d23061ab028
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Oct 26 15:17:26 2002 +0000

    - Committed Marco's block rewrite:

       + Blocks are not longer called if not rendered: major performance
         improvement.
       + Fixed some bugs (preview option was broken, path option was broken).
       + Removed "ascii"-type blocks.
       + Added permission to for "PHP blocks"
       + ...

      NOTES:

       + You'll want to run "update.php":

           ALTER TABLE blocks DROP remove;
           ALTER TABLE blocks DROP name;

       + You'll want to update your custom modules as well as the modules in
         the contrib repository.  Block function should now read:

           function *_block($op = "list", $delta = 0) {
             if ($op == "list") {
               return array of block infos
             }
             else {
               return subject and content of $delta block
             }
           }