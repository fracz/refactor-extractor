commit e88dfb734a64aad4dbd402642816781b0e1725ec
Author: Misko Hevery <misko@hevery.com>
Date:   Sat Nov 12 15:23:30 2011 -0800

    refactor(injector): $injector is no longer a function.

    - $injector('abc') -> $injector.get('abc');
    - $injector(fn) -> $injector.invoke(null, fn);