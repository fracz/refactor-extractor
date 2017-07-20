commit 2952d16f78eea91724760f526504010cb8f796a9
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Jan 20 12:20:31 2001 +0000

    - updated/improved discussion engine: it needs additional fine-tuning
      though but I think this is stable enough for public consumption and
      real-life testing.
       ==> a first big step towards a flexible comment engine.

    IMPORTANT:
      - Required theme updatins:

        UnConeD: check your $theme->controls() as I added a very, very
                 dummy implementation

      - Required database updates:

        alter table users modify mode tinyint(1) DEFAULT '' NOT NULL;
        alter table comments change sid lid int(6) DEFAULT '0' NOT NULL;
        alter table comments add link varchar(16) DEFAULT '' NOT NULL;
        update comments set link = 'story';