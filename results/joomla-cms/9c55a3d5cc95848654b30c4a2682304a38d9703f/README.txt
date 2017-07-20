commit 9c55a3d5cc95848654b30c4a2682304a38d9703f
Author: dianaprajescu <diana.prajescu@gmail.com>
Date:   Fri Jun 22 19:20:46 2012 +0300

    Start writing the oauth 1.0a client for Twitter.

    Twitter authentication draft.

    Working OAuth.

    Add POST request to sendRequest() method. Posting tweets works :-).

    Add getOption() and setOption() methods to JTwitterObject. Change JTwitter 'api.url' option.

    Add tweet() method parameters. OAuth improvements.

    Add code in oauthRequest to validate the response. Change the error string in JTwitterStatusesTest.

    Twitter uses only OAuth, remove code for basic authentication in fetchUrl(). Remove unused constants in JTwitterHttp.

    Remove sendRequest() $code parameter, it is not needed. 200 is the only code for a successful request, anything else is an error.

    Tests for tweet() method.

    Add setToken() method to JTwitterOAuth.

    Remove $code from sendRequest method.