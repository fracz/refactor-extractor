commit f3460297a962e2a3d07869a56241866576ffc7a3
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Aug 9 19:42:57 2012 +0100

    MDL-32188 question engine: behaviour static methods -> new classes

    It was always a bit of a hack to use static methods on the
    qbehaviour_whatever classes to return metadata about the behaviour. It
    is better design to have real qbehaviour_whatever_type classes to report
    that metadata, particularly now that we are planning to add more such.

    For example, inheritance works better with real classes. See, for
    example, the improvements in
    question_engine::get_behaviour_unused_display_options().

    This change has been implemented in a backwards-compatbile way. Old
    behaviours will continue to work. There will just be some developer debug
    output to prompt people to upgrade their code properly.