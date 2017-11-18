commit 98d54aed2c68e52c83b6a2369b73ba96e5accb39
Author: Christopher Manning <manning@stanford.edu>
Date:   Tue Jun 10 17:13:28 2014 -0700

    Documentation improvements (no functional changes). Make it so there is only one copy of the documentation of the options to PTBTokenizer/PTBLexer, or else they are always out of sync. Leave it in PTBTokenizer, since PTBLexer is not a public class. Add what the default is for each option.