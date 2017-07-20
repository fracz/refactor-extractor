commit a6316f020e47796b6942d8ada8d6b7641b862d4b
Author: Phil Taylor <phil@phil-taylor.com>
Date:   Tue Jan 10 13:36:56 2017 +0000

    Fix “You are not authorised to view this” when mod_expires enabled (#13516)

    * Fix “You are not authorised to view this” when mod_expires enabled

    Closes #8731 Dec 18, 2015
    Closes #8757 Dec 21, 2015
    Closes #9013 Jan 28, 2016
    Closes #9145 Feb 17, 2016
    Closes #9615 Mar 26, 2016
    Closes #10753 Jun 7, 2016

    * code style

    * Unit test refactoring (cannot do it the old way as relative dynamic dates in headers)

    * Remove duplicate header output for charset

    * Better Unit Tests

    * More unit tests :)

    * Close after setting headers

    * Do not cache the response to this, its a redirect