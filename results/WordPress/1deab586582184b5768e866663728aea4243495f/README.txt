commit 1deab586582184b5768e866663728aea4243495f
Author: Daryl Koopersmith <dkoopersmith@gmail.com>
Date:   Fri Oct 5 04:23:59 2012 +0000

    Use the new media modal to insert galleries into TinyMCE and the text editor.

    '''Galleries'''

    * Gallery insertion from the new media modal (into TinyMCE, the text editor, etc).
    * Gallery previews in TinyMCE now use the `wp.mce.views` API.
    * Disables the TinyMCE `wpgallery` plugin.
    * Gallery previews consist of the first image of the gallery and the appearance of a stack. This will later be fleshed out to include more images/functionality (including editing the gallery, gallery properties, and showing the number of images in the gallery).
    * Multiple galleries can be added to a single post.
    * The gallery MCE view provides a bridge between the `wp.shortcode` and `Attachments` representation of a gallery, which allows the existing collection to persist when a gallery is initially created (preventing a request to the server for the query).


    '''Shortcodes'''

    * Renames `wp.shortcode.Match` to `wp.shortcode` to better expose the shortcode constructor.
    * The `wp.shortcode` constructor now accepts an object of options instead of a `wp.shortcode.regexp()` match.
    * A `wp.shortcode` instance can be created from a `wp.shortcode.regexp()` match by calling `wp.shortcode.fromMatch( match )`.
    * Adds `wp.shortcode.string()`, which takes a set of shortcode parameters and converts them into a string.* Renames `wp.shortcode.prototype.text()` to `wp.shortcode.prototype.string()`.
    * Adds an additional capture group to `wp.shortcode.regexp()` that records whether or not the shortcode has a closing tag. This allows us to improve the accuracy of the syntax used when transforming a shortcode object back into a string.

    '''Media Models'''

    * Prevents media `Query` models from observing the central `Attachments.all` object when query args without corresponding filters are set (otherwise, queries quickly amass false positives).
    * Adds `post__in`, `post__not_in`, and `post_parent` as acceptable JS attachment `Query` args.
    * `Attachments.more()` always returns a `$.promise` object.

    see #21390, #21809, #21812, #21815, #21817.


    git-svn-id: http://core.svn.wordpress.org/trunk@22120 1a063a9b-81f0-0310-95a4-ce76da25c4cd