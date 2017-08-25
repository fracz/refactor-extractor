commit b24d91954b3e643fa93997b290b41c48da574647
Author: Andreas Gohr <gohr@cosmocode.de>
Date:   Thu Feb 18 12:19:07 2016 +0100

    refactor page saving and introduce COMMON_WIKIPAGE_SAVE

    This makes the saveWikiText() function a little easier to read and moves
    external edit handling to its own function. Behavior stays the same
    (tests are unchanged).

    In addition a new event COMMON_WIKIPAGE_SAVE is introduced that makes
    intercepting and acting on page saves much easier than possible before.

    Developers can:

    * prevent saves by either preventing the default action or overwriting
      the contentChanged field in a BEFORE hook
    * enforce saves even when no content changed by overwriting the
      contentChanged field in a BEFORE hook
    * Adjust the saved content by modifying the newContent field in a BEFORE
      hook
    * Adjust the stored change log information (summary, type, extras) in an
      AFTER hook
    * Easily know if a page was deleted, created or edited by inspecting the
      changeType field
    * what ever they want before or after a wiki page is saved