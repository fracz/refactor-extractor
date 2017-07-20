commit 38c19a5fde31671e5bfb7785e359bfe6e29871e3
Author: Lance Willett <nanobar@gmail.com>
Date:   Thu Jun 6 00:22:09 2013 +0000

    Twenty Thirteen: improve backward compatibility escape hatch for pre-3.6 versions:
     * Switch to the built-in method of providing a back link in `wp_die()` for the Customizer
     * Use WP_DEFAULT_THEME for both arguments during theme switching to simplify the logic
     * Prevent the 3.3 and earlier theme preview action gracefully

    Props obenland for patches, fixes #24441.

    git-svn-id: http://core.svn.wordpress.org/trunk@24411 1a063a9b-81f0-0310-95a4-ce76da25c4cd