commit 924adaec6c9f955d0207e2ee84652aae70175674
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Wed Jan 4 17:51:58 2017 +0100

    Render reference documentation with Asciidoctor

    This commit removes docbook from the documentation toolchain and
    instead makes use of asciidoctor to render the reference documentation
    in HTML and PDF formats.

    The main Gradle build has been refactored with the documentation tasks
    and sniffer tasks extracted to their own gradle file in the "gradle"
    folder.

    A new asciidoctor Spring theme is also used to render the HTML5 backend.

    Issue: SPR-14997