commit 26e81cf14bf32585742cbb75ac14045acaab2692
Author: Dan Sandler <dsandler@android.com>
Date:   Tue May 6 10:01:27 2014 -0400

    Quantum notification improvements.

    New API introduced here: Notification.color (and
    Builder.setColor()), allowing apps to specify an accent
    color to be used by the template. The Quantum templates
    (which are now the only kind we support) use this when
    creating a circular background to draw behind the smallIcon
    in the expanded form.

    Additionally, the quantum and legacy templates are no longer
    in superposition; all apps using Builder will get quantum.

    Change-Id: Iac5e2645cc5c2346ed458763f2280ae9c6368b62