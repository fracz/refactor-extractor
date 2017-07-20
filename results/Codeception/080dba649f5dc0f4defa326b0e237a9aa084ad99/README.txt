commit 080dba649f5dc0f4defa326b0e237a9aa084ad99
Author: Michael Bodnarchuk <DavertMik@users.noreply.github.com>
Date:   Sat Jul 23 11:47:56 2016 +0300

    Yii2 module improvements (#3343)

    Improved Yii2 module:

    * init part added to work with unit/acceptance tests
    * added entryScript and entryUrl config values
    * added amOnRoute method
    * added amloggedAs method
    * added fixtures support
    * added grabComponent method
    * added mocked mailer component
    * grabLastSentEmail should return actual last email
    * created logger in debug mode,
    * mocked assetManager