commit eca230b52197bec87bee059d15651a3886dd62f8
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Mar 28 16:05:34 2013 +0000

    MDL-38538 question unit tests: improve things a bit.

    1. Split the question_attempt tests into one class per file.
    2. Imporve the API to give tests more control, and to test more of the
       important code. Some of this is not used here, but it is about to be.