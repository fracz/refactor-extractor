commit fc406430331b1c681193b087998e8336e4a61445
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue May 31 14:55:50 2016 -0400

    Optimize Consumes/ProducesRequestCondition

    Before this change Consumes/ProducesRequestCondition shared a common
    match method in the package private AbstractMediaTypeExpression. The
    benefit, two lines of code, was negligible but was forcing each
    condition into parsing the content type of the request body or
    evaluating the content type for the response respectively.

    This change removes the shared match method and brings it down into
    each sub-class resulting in a performance improvement as well as in
    simpler code including exception handling.

    Issue: SPR-14299