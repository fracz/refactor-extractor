commit efa5155a2a465fa4049e40f78d8eb48fa347931c
Author: Ray Morris <Ray.Morris@teex.tamu.edu>
Date:   Wed Jan 8 14:08:40 2014 -0600

    MDL-40313 questionbank: Add question filtering API

    Add new API for filtering questions, refactoring the options to display
    old questions and include subcategories into new
    question_bank_search_condition classes. Make the new API pluggable via
    local_[pluginname]_get_question_bank_search_conditions.