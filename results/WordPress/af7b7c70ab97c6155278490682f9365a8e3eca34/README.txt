commit af7b7c70ab97c6155278490682f9365a8e3eca34
Author: iandstewart <iandstewart@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Tue Jun 7 21:28:56 2011 +0000

    Twenty Eleven: miscellaneous bug fixes and improvements
    * Only style images uploaded to the WordPress media library with border styles (and not placeholder images for content added by plugins)
    * Prevent content added by plugins and long custom excerpts from overflowing the featured post slider
    * Make sure small thumbnails will never appear in the Custom Header area
    * Better indicate the current featured post with custom link color and cursor
    * Add a class for text-only featured posts
    * Style improvements for small screens


    git-svn-id: http://svn.automattic.com/wordpress/trunk@18181 1a063a9b-81f0-0310-95a4-ce76da25c4cd