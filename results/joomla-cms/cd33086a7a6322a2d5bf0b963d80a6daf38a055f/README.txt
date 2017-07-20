commit cd33086a7a6322a2d5bf0b963d80a6daf38a055f
Author: Fedir Zinchuk <getthesite@gmail.com>
Date:   Tue Dec 6 23:48:05 2016 +0200

    TinyMCE drag&drop toolbar editing (#11926)

    * tinymce initialization, use addScriptOptions and tinymce-init.js

    * Finalize tinymce-init.js

    * Small improve for code duplication

    * Allow multiple editors with diferent options

    * Setup default options for the editor script only once

    * Minified version of tinymce-init

    * code style

    * Improve a bit

    * add TinymceBuilder

    * base markup for drag-drop

    * drag in / drag out

    * tinymce-config leveloptions form

    *  tinymce-builder.js

    * Render buttons with javascript

    * Allow to set values from the preset, and make extra options form work

    * Clear the forms

    * clearPane

    * Enable tooltip for buttons

    * Move buttons and presets list to PlgEditorTinymce class

    * Get options depend from user ViewLevel

    * Make it work

    * Make text inputs usable

    * c.s. a bit

    * Use Sets

    * highlight the drop area

    * TinyMCE buttons translation

    * Add color to preset butons

    * Some more highlight for the drop area

    * Update the presets to be more close to what we have now, for B/C

    * Do not ignore a group separator, update advanced preset and fix resizing

    * Update the field label

    * Update call of JHtml for a 'script' and 'stylesheet'