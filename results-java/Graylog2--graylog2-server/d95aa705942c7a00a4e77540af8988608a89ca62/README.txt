commit d95aa705942c7a00a4e77540af8988608a89ca62
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Dec 2 11:50:44 2015 +0100

    improve CORS performance

     - answer OPTIONS requests early
     - include Access-Control-Max-Age header to allow caching the CORS preflight in browser that support this

    this greatly reduces the number of requests necessary to request data for the UI