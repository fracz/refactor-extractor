commit e3ce6c9048d95e6e19b02ccb209f17d28543170c
Author: Robert Muir <rmuir@apache.org>
Date:   Thu May 5 18:31:48 2016 -0400

    Painless: add fielddata accessors (.value/.values/.distance()/etc)

    This gives better coverage and consistency with the scripting APIs, by
    whitelisting the primary search scripting API classes and using them instead
    of only Map and List methods.

    For example, accessing fields can now be done with `.value` instead of `.0`
    because `getValue()` is whitelisted. For now, access to a document's fields in
    this way (loads) are fast-pathed in the code, to avoid dynamic overhead.

    Access to geo fields and geo distance functions is now supported.

    TODO: date support (e.g. whitelist ReadableDateTime methods as a start)
    TODO: improve docs (like expressions and groovy have for document's fields)
    TODO: remove fast-path hack

    Closes #18169

    Squashed commit of the following:

    commit ec9f24b2424891a7429bb4c0a03f9868cba0a213
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu May 5 17:59:37 2016 -0400

        cutover to <Def> instead of <Object> here

    commit 9edb1550438acd209733bc36f0d2e0aecf190ecb
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu May 5 17:03:02 2016 -0400

        add fast-path for docvalues field loads

    commit f8e38c0932fccc0cfa217516130ad61522e59fe5
    Author: Robert Muir <rmuir@apache.org>
    Date:   Thu May 5 16:47:31 2016 -0400

        Painless: add fielddata accessors (.value/.values/.distance()/etc)