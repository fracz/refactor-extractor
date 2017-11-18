commit 9496ca0309b892e4784420b3eaf25483ac55b603
Author: adammurdoch <a@rubygrapefruit.net>
Date:   Mon Nov 29 07:20:19 2010 +1100

    Reworked the mechanism for extracting DSL meta-data from the API source code.
    - extracts all super-interfaces of a type (rather than most super-interfaces)
    - improved resolution of names in {@link} tags.
    - handle case where an overridden method has {@inheritDoc}