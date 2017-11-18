commit 9511c76a2eb6cdfbd8f7d159359fc24fad90d786
Author: Ariel Coppes <ariel.coppes@gemserk.com>
Date:   Sun Nov 4 17:55:28 2012 -0200

    modified Net API with some of the discussed changes, removed the createHttpRequest() method, modified processHttpRequest to sendHttpRequest(), added timeOut to the HttpRequest, added canceled() method to HttpResponseListener which must be called if the request was cancelled (probably by a timeout), error(code) wasnt added since HttpResponse already has the status code and that could be processed inside handleHttpResponse(HttpResponse), added input stream methods (maybe javadocs could be improved a bit)