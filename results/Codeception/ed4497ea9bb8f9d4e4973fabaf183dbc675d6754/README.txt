commit ed4497ea9bb8f9d4e4973fabaf183dbc675d6754
Author: Gintautas Miselis <naktibalda@gmail.com>
Date:   Mon Jun 6 21:52:38 2016 +0100

    Fixed URL matching in WebDriver::seeLink (#3170)

    * Throw better exception if seeLink did not found any links matching given text

    * [WebDriver][InnerBrowser] fixed URL matching in WebDriver::seeLink, improved error messages of seeLink and dontSeeLink