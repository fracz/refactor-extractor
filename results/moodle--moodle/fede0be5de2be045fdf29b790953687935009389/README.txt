commit fede0be5de2be045fdf29b790953687935009389
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Jun 27 15:27:50 2012 +0100

    MDL-34065 lib: improve two debugging messages.

    If the string passed to get_string is empty, say that. Don't say that it
    contains illegal characters.

    When relying on the __call magic in plugin_renderer_base, when the
    method cannot be found, include the right class name in the error
    message.