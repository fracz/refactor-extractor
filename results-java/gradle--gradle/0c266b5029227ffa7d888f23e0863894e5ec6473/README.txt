commit 0c266b5029227ffa7d888f23e0863894e5ec6473
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Mon Jan 9 19:28:10 2012 +0100

    reworked codenarc plugin

    notably:
    - turn into its own plugin
    - introduce checkstyle configuration (don't hardcode the codenarc version)
    - configure via extension object
    - improve tests and move them into code-quality project
    - implement in groovy