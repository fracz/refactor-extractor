commit e06871ef175227305f6c0f09d1d63fccd9b9fdf0
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Mar 27 18:29:03 2017 -0400

    Internal RequestMappingHandlerAdapter refactoring

    Extract controller method caches including associated code and
    discovery of @ControllerAdvice components into a separate, package
    private helper class (ControllerMethodResolver).