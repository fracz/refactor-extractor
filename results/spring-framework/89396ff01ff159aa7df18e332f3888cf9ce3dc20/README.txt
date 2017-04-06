commit 89396ff01ff159aa7df18e332f3888cf9ce3dc20
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Jun 29 15:51:05 2016 -0400

    Refactor handleNoMatch for @RequestMapping

    Originally handleNoMatch looked for partial matches based on URL
    pattern, HTTP method, consumes, produces, and params in that order
    but without narrowing down the set of partial matches resulting in
    potentially inaccruate response status codes

    Commit 473de0 added an improvement to narrow the set with partial
    matches for URL pattern and HTTP method matches.

    This commit overhauls handleNoMatch so that the narrowing down of
    matches happens at each stage resulting in more accurate error
    reporting for request mappings with fine-grained conditions.

    Issue: SPR-14397