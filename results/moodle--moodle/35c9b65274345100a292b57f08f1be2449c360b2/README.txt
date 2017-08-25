commit 35c9b65274345100a292b57f08f1be2449c360b2
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Fri Jun 17 14:55:51 2011 +0100

    MDL-24594 Fix some issues with the display of HTML choices.

    The most significant issue is that the HTML editor alwasy wraps <p> tags round the input, but that is not appropriate for the choices. It is especially not appropriate because we want to display the choices in a <lable> for accessibility and usability reasons. In valid HTML label can only contain inline elemnts. Therefore, I introduced a make_html_inline method, with a minimal implementation. (It could be improved in future.)

    Long term, I think the best option would be a new form field type, editorinline, or something like that. That would be a smaller version of TinyMCE that only lets you enter inline elements.