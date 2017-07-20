commit 8ed022b568475672b0b18985b687da21af11ee19
Author: Joe McGill <joemcgill@gmail.com>
Date:   Wed Oct 19 03:06:28 2016 +0000

    Media: Remove `alt` fallbacks to improve accessibility.

    This removes the fallbacks in `wp_get_attachment_image()` and in
    `wp.media.string.props` which attempt to generate an `alt` value
    from the image caption or title if an `alt` attribute isn't explicitly
    set.

    This allows for image HTML to be generated that contains an empty `alt`
    value, i.e., `alt=""` which is much preferable for screen readers than
    reading redundant content in the case of a caption, or when reading the
    image title, which is often generated from the filename and not helpful
    as `alt` text.

    Props odie2, joedolson, rianrietveld, afercia, iamjolly, joemcgill.
    Fixes #34635.
    Built from https://develop.svn.wordpress.org/trunk@38812


    git-svn-id: http://core.svn.wordpress.org/trunk@38755 1a063a9b-81f0-0310-95a4-ce76da25c4cd