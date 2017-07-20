commit 270a1dfc2275f439f3ae180be9dc125bc0e1c7c5
Author: Sven Hurt <kontakt@sven-hurt.de>
Date:   Mon May 16 18:20:00 2016 +0200

    Change scheme for the google maps api call to https and include an api key if one was set (#10439)

    * Change api scheme to https

    While the main change to this file is about setting the scheme to https, I've also refactored the uri handling and added the option to apply an api key as well.

    * Remove urlencode from address since it will be handled automatically in toString()

    * Create a new JUri instance instead of using $this->uri to prevent errors with e.g. isSercure()

    * Remove the sensor parameter since it doesn't seem to be need any longer

    Remove the sensor parameter since it doesn't seem to be need any longer. Thanks for pointing it out @robert.

    * Satisfy Travis, maybe?!

    * Add the sensor parameter back to the api url since the tests seem to rely on it

    Add the sensor parameter back to the api url since the tests seem to rely on it. If that really is the case, we should implement be a better way of "parsing" the url and / or looking for an array index in the tests.

    * Fix url parsing to properly handle query params

    * Remove the sensor parameter since it doesn't seem to be needed any more

    This time the test(s) shouldn't fail - fingers crossed.