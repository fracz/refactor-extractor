commit f930a50805a03ae36b928421873d361f51f2703d
Author: Marc Würth <ravage@bluewin.ch>
Date:   Wed Jun 26 13:36:50 2013 +0200

    Fix for #3318

    Fixes https://cakephp.lighthouseapp.com/projects/42648-cakephp/tickets/3318

    It seems fixing this in the htaccess file(s) isn't going to work even though a url rewriting based solution was more clean. On the plus side this works for any web server.
    If a url is called with "index.php" in it then the CakeRequest swallows this part and fixes the path. Any linked url from the requested page will have a clean url. Thus after following one of these urls this problem is gone anyway.

    Some code docblock improvements to CakeRequestTest.php
    Added test case for fix
    Also now you can call just index.php even if you have url rewriting enabled