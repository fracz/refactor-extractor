commit e6401b29e68067c09db04991ec7096afc1b790a9
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Feb 20 18:26:31 2017 -0500

    Access to request and response byte[] in WebTestClient

    The WiretapConnector now decorated the ClientHttpRequest & Response
    in order to intercept and save the actual content written and read.

    The saved content is now incorporated in the diagnostic output but may
    be used for other purposes as well (e.g. REST Docs).

    Diagnostic information about an exchange has also been refactored
    similar to command line output from curl.