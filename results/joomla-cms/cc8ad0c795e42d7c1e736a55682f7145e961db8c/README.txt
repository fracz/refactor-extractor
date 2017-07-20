commit cc8ad0c795e42d7c1e736a55682f7145e961db8c
Author: Izhar Aazmi <izharaazmi@gmail.com>
Date:   Thu Feb 2 22:11:44 2017 +0530

    [Admin Menu] Fix showing of protected menu items in container type menu item (#13830)

    * Menutype ‘menu’ is no longer protected. Do not allow batch/trash/delete for protected menu items.

    * Show protected menu items on explicit filter only

    * Container menutype improvements and other improvements related to menu client_id.

    * Child item fix

    * Fix root exclusion and node visibility when in disabled state.

    * Update test db stub for jos_menu_types

    * Remove protected items filter from manager and other fixes.

    * An attempt towards UX improvement

    * Language update

    * Update language a/c to better suggestion by @infograf768

    * Use only ‘main’ and not ‘menu’ menutype to load components menu items under container menu item

    * Removed extra parenthesis