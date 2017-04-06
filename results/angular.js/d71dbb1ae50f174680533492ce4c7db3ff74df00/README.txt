commit d71dbb1ae50f174680533492ce4c7db3ff74df00
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Mon Apr 28 22:23:10 2014 +0200

    refactor(jqLite): stop patching individual jQuery methods

    Currently Angular monkey-patches a few jQuery methods that remove elements
    from the DOM. Since methods like .remove() have multiple signatures
    that can change what's actually removed, Angular needs to carefully
    repeat them in its patching or it can break apps using jQuery correctly.
    Such a strategy is also not future-safe.

    Instead of patching individual methods on the prototype, it's better to
    hook into jQuery.cleanData and trigger custom events there. This should be
    safe as e.g. jQuery UI needs it and uses it. It'll also be future-safe.

    The only drawback is that $destroy is no longer triggered when using $detach
    but:

      1. Angular doesn't use this method, jqLite doesn't implement it.
      2. Detached elements can be re-attached keeping all their events & data
         so it makes sense that $destroy is not triggered on them.
      3. The approach from this commit is so much safer that any issues with
         .detach() working differently are outweighed by the robustness of the code.

    BREAKING CHANGE: the $destroy event is no longer triggered when using the
    jQuery detach() method. If you want to destroy Angular data attached to the
    element, use remove().