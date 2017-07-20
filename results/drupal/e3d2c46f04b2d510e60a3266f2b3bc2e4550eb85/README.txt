commit e3d2c46f04b2d510e60a3266f2b3bc2e4550eb85
Author: Kjartan Mannes <kjartan@2.no-reply.drupal.org>
Date:   Tue Mar 5 20:15:17 2002 +0000

    - applied search patch.
    - added who is online block.
    - made weblog module more configurable.
    - users may now delete their own accounts (Feature #8)
    - users may now request a password using email address *or* username.
      formerly required both items to match an account which was onerous.
    - the link to request a new password is now presented whenever a user
      fails login.
    - there is now a confirmation message after submitting edits to your
      user information.
    - error messages in user.module may now be stylized by themes.
    - <hook>_form has a $param setting you can fill with form parameters.
    - improved wording for a few config settings.
    - fixed various non-coding standard things.