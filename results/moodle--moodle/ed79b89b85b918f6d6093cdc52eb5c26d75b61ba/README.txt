commit ed79b89b85b918f6d6093cdc52eb5c26d75b61ba
Author: David Mudrak <david@moodle.com>
Date:   Fri Feb 18 17:33:37 2011 +0100

    MDL-26010 Reformatting SQL of the previous Andrew's fix

    I have reformatted the SQL according to the style used in Moodle code to
    improve readability of the query. Added sql_compare_text() to the right
    side of the comparison, even though the current callers provide integers
    here (just in case).