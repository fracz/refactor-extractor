commit 2eebde6e5fd3138e775a2e35124df44aba885191
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Sun May 16 11:45:33 2010 +0000

    MDL-22015 core_string_manager: caching and performance logging improved

    The results of merged en + en_local + parentlang + parentlang_local +
    lang + lang_local are now saved into disk cache in dataroot/cache/lang/.
    The number of get_string() calls, and number of mem cache and disk cache
    hits are part of performance logging.
    Disk cache must be removed whenever the language packs or their local
    customizations are deleted. Disk cache is rebuilt automatically.