commit 51a3d0ba6a5f5d97887f6d424e411deffd53ed61
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Nov 10 10:47:03 2010 +0100

    refactored session configuration

    The configuration names have been changed to avoid confusion (user was
    ambiguous)

        Before:
              <app:user default_locale="fr">
                  <app:session name="SYMFONY" type="Native" lifetime="3600" />
              </app:user>

        After:
              <app:session default_locale="fr" name="SYMFONY" storage_id="native" lifetime="3600" />