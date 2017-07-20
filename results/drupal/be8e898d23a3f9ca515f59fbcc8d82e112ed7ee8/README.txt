commit be8e898d23a3f9ca515f59fbcc8d82e112ed7ee8
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat May 5 13:57:29 2001 +0000

    - Uhm.  Rewrote the module system: less code clutter, less run-time
      overhead, and a lot better (simpler) module API.  I had to edit a
      LOT of files to get this refactored but I'm sure it was worth the
      effort.

      For module writers / maintainers:

      None of the hooks changed, so 95% of the old modules should still
      work.  You can remove some code instead as "$module = array(...)"
      just became obsolete.  Also - and let's thank God for this - the
      global variable "$repository" has been eliminated to avoid modules
      relying on, and poking in drupal's internal data structures.  Take
      a look at include/module.inc to investigate the details/changes.

    - Improved design of the content modules "story", "book" and "node"
      (to aid smooth integration of permisions + moderate.module).  I'm
      still working on the permissions but I got side tracked for which
      I "Oops!".