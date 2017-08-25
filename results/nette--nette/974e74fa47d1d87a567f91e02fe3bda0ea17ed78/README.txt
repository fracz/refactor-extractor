commit 974e74fa47d1d87a567f91e02fe3bda0ea17ed78
Author: David Grudl <david@grudl.com>
Date:   Wed Apr 30 00:50:55 2008 +0000

    - improved Nette::Caching, added namespaces, TemplateStorage, new tests
    - Nette completely depends on autoloading now
    - some improvements in Nette::Loaders
    - removed Nette::Loaders::RobotLoader::$cacheFile (in favour of object Cache)
    - Nette::ServiceLocator behaviour modified to be more like 'registry' instead of 'lightweight container'
    - added Nette::Web::Uri - URI/URL storage
    - URI accessors moved from Nette::Web::HttpRequest to Nette::Web::Uri, use HttpRequest::getUri() and getOriginalUri()
    - added Nette::Web::HttpRequest::getReferer()