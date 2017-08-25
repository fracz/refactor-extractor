commit c00eb56ca033eaf999a45a1d39d78ac921d2c25a
Author: David Grudl <david@grudl.com>
Date:   Wed Jun 25 12:01:59 2008 +0000

    - added Application::$catchExceptions
    - improved service handling in Nette::Application::Application
    - Presenter caches $router, $httpRequest, $httpResponse for better performance
    - Template::render() have no parameters now (use Template::subTemplate())
    - added static Template::setCacheStorage() (use instead of Template::setCache())
    - on localhost, Templates are expired in 1 second by default
    - improved filter TemplateFilters::curlyBrackets