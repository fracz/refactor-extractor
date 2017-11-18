commit e4a437581552e3c55844a2d5f224623c5ae1fb83
Author: Rene Groeschke <rene@gradle.com>
Date:   Mon Apr 10 16:37:16 2017 +0200

    Address first review feedback.

    - improve test coverage on requesting explicit task via path
    - simplify handling of requests without declared filters using
    Specs.satisfiesAll

    +review REVIEW-6498