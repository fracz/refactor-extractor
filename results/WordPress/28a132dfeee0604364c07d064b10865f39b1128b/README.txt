commit 28a132dfeee0604364c07d064b10865f39b1128b
Author: Joe McGill <joemcgill@gmail.com>
Date:   Mon Jul 18 02:14:29 2016 +0000

    Media: Prevent `image_get_intermediate_size()` from returning cropped images.

    When `$size` is passed to `image_get_intermediate_size()` as an array of width
    and height values and an exact image size matching those values isn't available,
    the function loops through the available attachment sizes and returns the
    smallest image larger than the requested dimensions with the same aspect ratio.

    The aspect ratio check is skipped for the 'thumbnail' size to provide a fallback
    for small sizes when no other image option is available. This resulted in a poor
    selection when the size requested was smaller than the 'thumbnail' dimensions
    but a larger size matching the requested ratio existed.

    This refactors the internals of `image_get_intermediate_size()` to ensure the
    'thumbnail' size is only returned as a fallback to small sizes once all other
    options have been considered, and makes the control flow easier to follow.

    This also introduces a new helper function, `wp_image_matches_ratio()` for
    testing whether the aspect ratios of two sets of dimensions match. This function
    is also now used in `wp_calculate_image_srcset()` during the selection process.

    Props flixos, joemcgill.
    Fixes #34384, #34980.
    Built from https://develop.svn.wordpress.org/trunk@38086


    git-svn-id: http://core.svn.wordpress.org/trunk@38027 1a063a9b-81f0-0310-95a4-ce76da25c4cd