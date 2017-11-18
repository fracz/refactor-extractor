commit f09c4e1cdb6e080af57ac996805de20d6e771491
Author: Simon Willnauer <simonw@apache.org>
Date:   Mon Feb 6 18:17:34 2017 +0100

    Expose `search.highlight.term_vector_multi_value` as a node level setting (#22999)

    This setting was missed in the great settings refactoring and should be exposed
    via node level settings.