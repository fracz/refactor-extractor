commit 456de639123ae3da6320bed6140ab69ac9925e74
Author: Igor Wiedler <igor@wiedler.ch>
Date:   Tue Aug 31 21:26:50 2010 +0200

    [feature/request-class] Refactor request classes to use autoloading

    All class names have been adjusted to use a phpbb_request prefix,
    allowing them to be autoloaded.

    Also introduces some improvements to autoloading in general.

    PHPBB3-9716