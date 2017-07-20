commit 769e718d2c679b9bbd04b0a263148a3650a0ab92
Author: David A. Kennedy <me@davidakennedy.com>
Date:   Thu Oct 27 22:09:40 2016 +0000

    Twenty Seventeen: Improve user and developer experience with the customizer integration

    * Rename customizer JS files to customize-preview.js and customize-controls.js to align with the core file naming and make it clearer where each file runs.
    * Only show the colorscheme_hue control when there's a custom color scheme.
    * Update preview JS handling for revised front page section handling, see below.
    * Remove all references to "Theme Customizer" in code comments. It hasn't been called that since before 4.0.
    * Clarify the purpose of the JS files by updated the code comments in the file headers.
    * Improve code readability.
    * Make the arbitrary number of front page sections filterable, for UI registration and output.
    * Rename twentyseventeen_sanitize_layout to twentyseventeen_sanitize_page_layout to be clearer about what it sanitizes in case child themes or plugins consider reusing it.
    * Rename page_options setting/control to page_layout as that's more reflective of what that option does; and again, helps for potential extensions.
    * Make the page layout option contextual to pages and the sidebar being inactive, as the option only applies when there is no sidebar (per its description).
    * Condense options into a single section.
    * Add selective refresh for front page sections.
    * Locate active_callback functions within customizer.php so that they're easier to find when editing customizer registrations, similarly to sanitize callbacks.
    * Adjust the styling for placeholders for panels that aren't active.
    * Ensure that the new visible edit shortcuts don't have any issues.

    Props celloexpressions.

    Fixes #38426.

    Built from https://develop.svn.wordpress.org/trunk@38986


    git-svn-id: http://core.svn.wordpress.org/trunk@38929 1a063a9b-81f0-0310-95a4-ce76da25c4cd