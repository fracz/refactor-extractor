commit 48f96b94d046f39abaa0708ffcf7c559ac79633f
Author: Edmundo Alvarez <github@edmundoa.com>
Date:   Wed Aug 10 18:10:09 2016 +0200

    Decorator improvements (#2654)

    * Fix decorator list styling

    Fixes #2598

    * Move decorators help to popover

    In that way we gain some additional spacing on the sidebar.

    * Improve add decorator styling

    Clean-up divs and improve margins

    * Move edit and delete buttons into actions dropdown

    The decorator list doesn't have much space, and this makes it look
    cleaner.

    * Do not add pipeline decorator without a pipeline

    Mark non-optional select values as required, avoiding submitting a form
    to create a pipeline decorator without selecting a pipeline.

    * Remove unused imports

    * Relax prop type in sortable list

    Message decorators pass react elements instead of text, but that should
    be allowed.

    * Fix message fields separation

    * Only show "show original" message button when decorated

    * Do not display message field actions when decorated

    Before this change, we didn't display message field actions for fields
    added by a message decorator, but we did when a field was altered by a
    decorator. This changes that behaviour, as decorated values will not
    work on searches, and cannot be extracted.

    * Add modified fields to search decoration stats

    We should also include changed and modified fields. And also show the
    actual added fields.

    * Do not show field analyzers for decorated fields

    Fields added by decorators did not show field analyzers, but modified
    fields did. This removes that, as it won't work.

    * Guard against null DecorationStats

    * Use a more explicit indicator for decorated fields

    Replace the pencil icon with some text that self explains why that field
    is special.