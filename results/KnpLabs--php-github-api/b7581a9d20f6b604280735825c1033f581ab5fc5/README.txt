commit b7581a9d20f6b604280735825c1033f581ab5fc5
Author: Alexander Holman <alexander@holman.org.uk>
Date:   Mon Nov 21 12:00:53 2016 +0000

    refactor:Make addMember an alias of add
    Duplicate code existed for adding member to an organisation

    Under lib/Github/Api/Organisation/Members.php the addMember and add
    methods returned the exact same code and used the exact same arguments.

    With that in mind I made addMember an "alias" returning $this->add
    passing the same arguments through.