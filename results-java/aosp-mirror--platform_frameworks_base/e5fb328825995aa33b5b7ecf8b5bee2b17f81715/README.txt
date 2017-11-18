commit e5fb328825995aa33b5b7ecf8b5bee2b17f81715
Author: Mitsuru Oshima <oshima@google.com>
Date:   Tue Jun 9 21:16:08 2009 -0700

    resolution support fix/improvement
        * adding compatibility menu
        * backup gravity
        * set expanable=true if the screen size is hvga * density.
        * added "supports any density" mode. I'll add sdk check later.
        * disallow to catch orientation change event if the app is not expandable. This
          was causing layout problem under non-expandable mode. I discussed this with Mike C
          and we agreed to do this approach for now. We'll revisit if this causes problem to
          a lot of applications.