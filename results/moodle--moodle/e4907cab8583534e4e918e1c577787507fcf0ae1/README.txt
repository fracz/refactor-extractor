commit e4907cab8583534e4e918e1c577787507fcf0ae1
Author: Petr Škoda <commits@skodak.org>
Date:   Fri Aug 10 19:07:51 2012 +0200

    MDL-33041 add base TinyMCE and moodle plugin urls

    We should never use ../../../.. to reference core TinyMCE or moodle TinyMCE plugins, this crate problems if we ever decide to create improved loaders.