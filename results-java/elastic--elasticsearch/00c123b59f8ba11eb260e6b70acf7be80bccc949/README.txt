commit 00c123b59f8ba11eb260e6b70acf7be80bccc949
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu Aug 18 22:41:25 2016 -0700

    Plugins: Remove IndexTemplateFilter

    How index templates match is currently controlled by the
    IndexTemplateFilter interface. It is pluggable, to add additional
    filter implementations to the default glob matcher.

    This change removes the IndexTemplateFilter interface completely. This
    is a very esoteric extension point, and not worth maintaining. Instead,
    any improvements should be made to all of our glob matching.