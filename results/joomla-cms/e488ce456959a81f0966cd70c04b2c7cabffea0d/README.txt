commit e488ce456959a81f0966cd70c04b2c7cabffea0d
Author: Fedir Zinchuk <getthesite@gmail.com>
Date:   Sat Oct 1 12:12:43 2016 +0300

    tinyMCE use document "scriptoptions", and allow to override tinyMCE Javascript parameters (#11157)

    * tinymce initialization, use addScriptOptions and tinymce-init.js

    * Finalize tinymce-init.js

    * Small improve for code duplication

    * Allow multiple editors with diferent options

    * Setup default options for the editor script only once

    * Minified version of tinymce-init

    * code style

    * Improve a bit