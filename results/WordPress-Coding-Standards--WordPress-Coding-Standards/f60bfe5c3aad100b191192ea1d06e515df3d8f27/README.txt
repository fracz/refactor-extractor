commit f60bfe5c3aad100b191192ea1d06e515df3d8f27
Author: Juliette <github_nospam@adviesenzo.nl>
Date:   Thu Aug 18 08:10:58 2016 +0200

    Streamline WPCS in file documentation. (#651)

    * Update the file and class @package tag and remove the @category tag.

    Follow the standard as described in https://www.phpdoc.org/docs/latest/references/phpdoc/tags/package.html

    The @category tag is considered deprecated. A hierarchical @package tag should be used instead.

    Ref: https://www.phpdoc.org/docs/latest/references/phpdoc/tags/category.html

    * Use correct version number for the newly deprecated classes.

    * Fix some - unintentional - parse errors in the test files.

    * Attempt to lead by example comment-wise in the unit tests.

    Capitalization, punctuation and some spelling.

    * Proper capitalization and punctuation for class end comments.

    * Remove `//end` comments for methods and conditionals < 35 lines.

    * Simplify the test file method doc blocks.

    * Various documentation fixes, largely based on PHPCS Docs output.

    * Make sure any function which will be passed the $phpcsFile has the right type hint.

    But only if it doesn't conflict with upstream function signatures.

    * Verify and update the docblocks for all files and classes.

    Based on Git file history:

    * Add @since tags to all classes.
    * Add @since tags to properties and methods if they weren't included from the start.
    * Verify/Fix a number of @author tags.
    * Remove old/copied over/incorrect PHPCS tags.
    * Where necessary improved/corrected the class description.

    Also:
    * Consistent tag order in class doc blocks.
    * Fix tag alignment.
    * Remove redundant explanation in unit test class doc blocks.
    * Sync the description line of the unit test class doc blocks.

    * Add handbook links to a number of sniffs.

    * Add reference to upstream sniff a sniff is based upon.

    Includes information on when the sniff was last synced with the upstream sniff if applicable and available.

    * Add @license tag and @link tag to the WPCS GH repo, to all file level doc blocks.

    License tag as per https://www.phpdoc.org/docs/latest/references/phpdoc/tags/license.html

    * Add the docs ruleset to the coding standard for WPCS itself.

    * Add @since class changelogs for PR #647.

    * Fix minor grammar error.

    * Updated `@since` tags for `2014-12-11` release to `0.3.0`.

    * Re-instate previously removed @since tags.

    * Updated based on feedback.

    * Adjusted the @package tags as per discussion in the PR thread
    * Removed the @link tag to the handbook at the file level doc block
    * Removed the @author tags