commit 65488b5aeb3c26cca3745e522ee69cdee668433e
Author: David Grudl <david@grudl.com>
Date:   Tue Sep 16 13:58:37 2008 +0000

    - canonicalization moved back to Presenter from Application! (is enabled by default)
    - implemented PresenterComponent parameters transferring between presenters
    - fixed bugs in parameter transferring
    - Route compares parameters with default values in case-insensitive mode
    - improved ConventionalRenderer and ServiceLocator