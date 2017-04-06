commit 42a5033c563fcb3a3f0ddd89ab62ec36d0e73996
Author: Igor Minar <igor@angularjs.org>
Date:   Wed Feb 6 15:25:28 2013 -0800

    chore(docs): improve docs parser type

    previously we barfed on function type definition with optional arguments
    like {function(number=)}

    this fixes it

    I also added a bunch of code that helps to debug incorrectly parsed docs.