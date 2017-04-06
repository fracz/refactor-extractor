commit 193d69c4d1264975bd755db11560345e59631f82
Merge: 8e6b0bb 448c03f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jan 3 10:57:45 2015 +0100

    feature #12594 [DX] [HttpKernel] Use "context" argument when logging route in RouterListener (iltar)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [DX] [HttpKernel] Use "context" argument when logging route in RouterListener

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT

    When using the "fingers_crossed" option, I only log stuff when a certain level is reached. I found the matched route with parameters to be extremely useful. The only problem was, that it gets dumped in a string, which reduces readability significantly when having multiple parameters.

    I've used the context argument to provide the additional information so it becomes easier to read. Especially for formatters that use the context, such as the HtmlFormatter, can really use this.

    *I've done a quick check and noticed that almost always the information is dumped in the message, while I think it should be in the context. Is this something that should be changed in general?*

    Commits
    -------

    448c03f [HttpKernel] RouterListener uses "context" argument when logging route