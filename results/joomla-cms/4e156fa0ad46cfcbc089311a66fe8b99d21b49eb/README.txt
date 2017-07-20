commit 4e156fa0ad46cfcbc089311a66fe8b99d21b49eb
Author: Izhar Aazmi <izharaazmi@gmail.com>
Date:   Tue Jan 17 16:11:40 2017 +0530

    Menu manager for Joomla Backend Menu (#13036)

    * Added client id column to menu_type table.
    Allow creating and editing of "menutype" records with client_id = 1
    Add client_id filters in menu and menu items list views
    Sync menu type filter and client_id filter allowing only menu type in the URL query parameter (B/C)
    Both Lists now also filtered by client id.
    Client id selection updates the menu type list options to show choices only for that client id

    TBD:
    Reserved menu types: main & menu

    * In modal list view we currently hide client_id filter and show only site menu types, will be updated once we have more clear vision.
    Menu type assignment to backend mod_menu config from both menu manager and module manager. Though that is not functional within the module itself.

    * Add/edit menu item redirect with clientId from list filter.
    Load menu item form based on active client id
    Menu type dropdown choices limited to active client id value
    Show menu-item-type choices (modal) trigger with client id parameter in the url
    Switch edit layout based on client id

    * Menu item type loading from component metadata xml or mvc not identifies backend and frontend application separately. Not yet able to load menu item type from backend so returns empty list. Front-end is still intact and unaffected.

    * Edit menu item and create menu item set to follow client id and menu type value consistenty.
    When creating menu item alias, the referenced menu must also belong to same client id.
    Client id field removed from form, this should be auto-calculated from the menu type when saving.

    * Adding layout metadata xml in backend to reference menu item types as it was in front-end.
    Removed unnecessary admin specific layout added earlier as it is so far same as original edit.php, may be added back when needed.
    Remove page specific meta data fields from backend component type menu items.
    For now disable/unsupport association for backend menu items.
    Disallow change of client id for existing menu items, unexpected conflicts may occur if allowed so better be safe.

    Ref to #2

    * Created each backend menu items using menu manager as a replica of existing Joomla backend menu. These are to be used for testing during upgrading menu module.
    language keys are not yet translated. Translation will be done as we are ready with most new or modified language keys application wide.
    Backend menu items does not require all those parameters as that with front-end menu items. Therefore segregated entire menu item xml for backend/frontend.

    Ref #2

    * [a11y] Protostar back to top (#12446)

    * [a11y] Protostar - back to top link

    * Oops Andre was right

    * add anchor for non-js enabled browsers

    * Restructure mod_menu to load preset menu items as an option (default). Other options will be the menu-type and will cause us to load from database. Ref #2

    * Disallow editing and set to home of protected menu type menu items viz. 'main' and 'menu'
    Allow explicit filtering by protected menu type choices in menu items list view. Not limited to #__menutypes table entries only. Unfiltered list still excludes those menu items. (B/C ok)
    Menu items created during installation of a component are now saved as published. When unpublished we won't load it in customised menu's component menu container. They will still be loaded irrespective of state as previously when preset is in use. (B/C ok)
    Home page can now be set one per client instead of one overall.
    Menu module only loads item from 'main' and 'menu' type menu items when requested for component menu items. This filter is now required because we are now going to have other custom menu types for backend which should not be included.

    Ref #2

    * Load menu items from databases in correct hierarchy. Remove any extra separator type menu items created due to exclusion of certain menu items based on various conditions.
    Populate menu items loaded from db into the AdminCssMenu object for final rendering.
    Load new installed components menu items dynamically under the specified menu item with “components container” flag. Any unpublished menu items from the protected menutypes (viz. “main” and “menu”) will be skipped.
    When loading from system preset menu items, these components menu items are all included regardless of their published state. (B/C ok).

    Ref #2

    * View manifests for menu item type and related language key updates. ref #2

    * Minor mistake fix.

    * Translate menu item titles in list view. Ref #2

    * Reset the preset menu structure to be same as the current J37 branch state, dropping implicit inclusion of joomla/joomla-cms#10657 improvement. Ref #2

    * Allow the existing components to leverage the menu/submenu entries in their install manifest for admin menu manager menu link types.
    This provides ability to create links for then without requiring them to add layout manifests. Hence, full B/C solution. Ref #2

    * Minor fix

    * Remove temporary dev phase files

    * Preparing for PR, database and install script updates.
    Ref #2

    * Minor fix

    * Codestyle fix

    * CS fix

    * Don’t sort menu items

    * Sort lang keys
    Allow ‘component’ as first level alias in admin menu items
    Fix lang key
    Remove ‘home’ setting from admin menu items

    * apply in hathor

    * menu item alias check for site only

    * Post merge fixes.

    * Fixes as suggested by @infograf768

    1. Group menu types by client id in lists and default admin menu
    2. Hide association tab for admin menu items.
    3. Hide client id filter for association mapping modal.

    * Add recovery mode for menu where the selected admin menu does not contain link to module manager and/or menu manager.

    * minor bug fix

    * Remove assoc column for admin menu items.
    Make recovery mode message more straight forward.
    Change radio to toggle buttons.