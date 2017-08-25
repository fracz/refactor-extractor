commit aff7c6a0ab4d3850037e39f7817c298a53c18215
Author: rodneyrehm <rodneyrehm@localhost>
Date:   Sat Oct 1 18:10:48 2011 +0000

    - improvement replaced most in_array() calls by more efficient isset() on array_flip()ed haystacks
    - added notes on possible performance optimization/problem with Smarty_Security