commit 8192ca8b9f379db31a7e3aa8f6b118e901a951fe
Author: David Grudl <david@grudl.com>
Date:   Fri Aug 29 17:14:44 2008 +0000

    - Nette::Objects: support for isXyz() getters, improved 'events' calling
    - Forms: new validator 'VALID'
    - Forms: removed ValidateException (because of big WTF factor)
    - Forms: added initial support for groups
    - Forms: some bug fixes, renamed getDisabled() -> isDisabled()