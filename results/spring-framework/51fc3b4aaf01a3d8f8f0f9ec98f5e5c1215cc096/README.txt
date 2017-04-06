commit 51fc3b4aaf01a3d8f8f0f9ec98f5e5c1215cc096
Author: Rossen Stoyanchev <rstoyanchev@gopivotal.com>
Date:   Sun May 18 19:20:58 2014 -0400

    Refactor @JsonView support w/ ResponseBodyInterceptor

    The newly added support for ResponseBodyInterceptor is a good fit for
    the (also recently added) support for the Jackson @JsonView annotation.

    This change refactors the original implementation of @JsonView support
    for @ResponseBody and ResponseEntity controller methods this time
    implemented as an ResponseBodyInterceptor.

    Issue: SPR-7156