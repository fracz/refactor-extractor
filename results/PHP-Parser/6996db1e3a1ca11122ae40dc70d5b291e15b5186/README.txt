commit 6996db1e3a1ca11122ae40dc70d5b291e15b5186
Author: Nikita Popov <nikita.ppv@googlemail.com>
Date:   Wed Apr 22 15:57:29 2015 +0200

    Build node attributes inside semantic action methods

    Minor performance improvement for parsing, also allows to access
    attributes with higher granulity in the parser, though this is not
    currently done.

    * #n can now be used to access the stack position of a token. $n
      is the same as $this->semStack[#n]. (Post-translate $n will
      actually be the stack position.)
    * $attributeStack is now $this->startAttributeStack and
      $endAttributes is now $this->endAttributes.
    * Attributes for a node are now computed inside the individual
      reduction methods, instead of being passed as a parameter.
      Accessible through the attributes() macro.