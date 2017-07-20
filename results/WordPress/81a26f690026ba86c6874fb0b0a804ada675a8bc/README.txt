commit 81a26f690026ba86c6874fb0b0a804ada675a8bc
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Wed Oct 26 08:07:30 2016 +0000

    Posts, Post Types: Add support for post type templates.

    WordPress has supported custom page templates for over 12 years, allowing developers to create various layouts for specific pages.
    While this feature is very helpful, it has always been limited to the 'page' post type and not was not available to other post types.

    By opening up the page template functionality to all post types, we continue to improve the template hierarchy's flexibility.

    In addition to the `Template Name` file header, the post types supported by a template can be specified using `Template Post Type: post, foo, bar`.
    When at least one template exists for a post type, the 'Post Attributes' meta box will be displayed in the back end, without the need to add post type support for `'page-attributes'`. 'Post Attributes' can be customized per post type using the `'attributes'` label when registering a post type.

    Props johnbillion, Mte90, dipesh.kakadiya, swissspidy.
    Fixes #18375.
    Built from https://develop.svn.wordpress.org/trunk@38951


    git-svn-id: http://core.svn.wordpress.org/trunk@38894 1a063a9b-81f0-0310-95a4-ce76da25c4cd