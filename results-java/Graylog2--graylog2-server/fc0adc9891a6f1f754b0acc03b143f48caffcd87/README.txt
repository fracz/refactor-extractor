commit fc0adc9891a6f1f754b0acc03b143f48caffcd87
Author: Edmundo Alvarez <github@edmundoa.com>
Date:   Mon Aug 15 12:11:16 2016 +0200

    More decorator improvements (#2674)

    * Refactor SearchSidebar

    We need to calculate the height for the sidebar when displaying
    decorators or field analyzers, so it's time to clean this up, as it's
    becoming too big and complicated.

    This splits up the field analyzer section of the search sidebar, so it's
    independent of the common controls.

    * Use consistent style when decorator is not loaded

    * Resolve entity IDs from decorator type definitions

    Attempt to resolve IDs from the decorator type definitions if possible.
    This allow us to display entity names (like we do in the configuration
    forms) instead of simply display an ID.

    * Make decorator list scrollable

    When there are many search decorators in the sidebar it is not possible
    to get to the non-visible ones.

    This change adapts the sidebar height in a similar way as we do for
    field analyzers, so at least it is possible to get to all the decorators
    that are applied to the search results.

    * Remove internal fields when search is decorated

    When a search is decorated, remove the internal fields from field set in
    the search result response.

    Fixes #2519

    * Keep decorator order after updated

    When a decorator configuration is updated, submit its previous order so
    the updated decorator still holds the same position.

    * Improve decorator sidebar scrolling

    - Move styles into css file
    - Give some padding to the scrollable component, so all options in the
      last decorator dropdown are still visible
    - Use more height for the sidebar

    * Fix typo

    * Simplify condition to extract entity ID from values

    * Simplify internal fields filtering