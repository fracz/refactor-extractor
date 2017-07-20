commit 5c9d92b7f86e006942dc8a7e9bdad993d7e27e62
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Wed Feb 24 03:56:24 2016 +0000

    Add Additional filters to Press This

    3 new filters that aim to improve the extensibility of Press This:
    1) `press_this_save_post_content` - Applied right after the side_load_images in order to allow potential side loads of other types of media.
    Example use case: side load non-image media, such as audio or video.

    2) `press_this_useful_html_elements`
    Allows filtering of currently hard coded array of HTML elements allowed in fetch_source_html step for special cases where additional HTML elements need to be kept.
    Example use case: HTML5 elements, such as amp-img, that someone wants to pull in.

    3) `press_this_suggested_content`
    A filter for the content right before it's passed to the editor and presented to the user.
    Example use case is when someone stored posts in a different, non-HTML format, such as Markdown, this is essential.

    Fixes #34455.
    Props cadeyrn, kraftbj


    Built from https://develop.svn.wordpress.org/trunk@36672


    git-svn-id: http://core.svn.wordpress.org/trunk@36639 1a063a9b-81f0-0310-95a4-ce76da25c4cd