commit fb058a7109e770d47b30d2ce560123f14216d5db
Author: Andrew Eddie <andrew.eddie@newlifeinit.com>
Date:   Thu Oct 29 06:13:11 2009 +0000

     ^ Renamed table jos_menu_template to jos_template_styles; refactored schema.
     ^ Renamed jos_menu.template_id to jos_menu.template_style_id.
     ^ Update milkyway template language file.
     ^ Update milkyway and bluestork manifest files.
     ^ Separated template styles from template file management/customisation.
     + Templates params now support multiple params groups (basic, advanced, etc).
     + Added ability to copy/clone an existing template style.

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@13380 6f6e1ebd-4c2b-0410-823f-f34bde69bce9