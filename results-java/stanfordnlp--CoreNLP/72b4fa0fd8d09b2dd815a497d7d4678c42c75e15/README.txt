commit 72b4fa0fd8d09b2dd815a497d7d4678c42c75e15
Author: Christopher Manning <manning@stanford.edu>
Date:   Wed Sep 18 16:36:20 2013 -0700

    Documentation improvements (no functional changes). Make it so there is only one copy of the documentation of the options to PTBTokenizer/PTBLexer, or else they are always out of sync. Leave it in PTBTokenizer, since PTBLexer is not a public class. Add what the default is for each option.