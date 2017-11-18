commit 90477b40a4b92c357184cc182f546ce742b1ffa1
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon May 1 21:26:16 2017 -0700

    Defer Charset.availableCharsets() call

    Change the `StringHttpMessageConverter` to defer calling
    Charset.availableCharsets() until absolutely necessary to help improve
    startup times.

    Issue: SPR-15502