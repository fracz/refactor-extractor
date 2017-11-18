commit ddc1397eba25e17591e3491dddb192b3fa38100f
Author: Andreas Gampe <agampe@google.com>
Date:   Fri Feb 26 16:54:59 2016 -0800

    Frameworks/base: Refactor TextView initialization

    To allow static initialization of a number of View classes based
    on TextView, refactor the initialization of the font cache to be
    explicit from the zygote.

    Bug: 27265238
    Change-Id: I1b71086d3f49d8b3e72eea2bf8359351d25fc0fd