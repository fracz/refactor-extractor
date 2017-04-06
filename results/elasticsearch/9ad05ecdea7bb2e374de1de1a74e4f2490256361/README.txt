commit 9ad05ecdea7bb2e374de1de1a74e4f2490256361
Author: Igor Motov <igor@motovs.org>
Date:   Sun Nov 4 20:31:46 2012 -0500

    lucene 4: make FieldVistors behave similar to FieldSelectors

    Added back reset() method for now to make things work. Will refactor it out when we have tests passing.