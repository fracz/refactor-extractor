commit 62ffeb4f0c5b2961ff683ef3ea5fbab04de73011
Author: PJ King <pking@sebts.edu>
Date:   Thu Aug 21 16:06:51 2014 -0400

    MDL-46652 custom_menu: fix for multi-language support breaks menu nesting

    Fixed bug by building full menu structure, then removing hidden menu items and all associated children.
    Refactored custom_menu_item class and convert_text_to_menu_nodes() to improve maintenance.