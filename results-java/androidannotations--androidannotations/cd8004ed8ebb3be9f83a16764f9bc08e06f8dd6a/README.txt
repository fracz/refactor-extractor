commit cd8004ed8ebb3be9f83a16764f9bc08e06f8dd6a
Author: Pierre-Yves Ricau <py.ricau@gmail.com>
Date:   Fri Feb 25 11:39:32 2011 +0000

    @Click and other listener methods may not be used twice for the same id.
    fixes issue 2
    large refactoring of validators, using delegation instead of inheritance, and factorization of a large code base