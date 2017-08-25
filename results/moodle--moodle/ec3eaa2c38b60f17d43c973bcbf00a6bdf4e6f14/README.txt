commit ec3eaa2c38b60f17d43c973bcbf00a6bdf4e6f14
Author: Paul Nicholls <paul.nicholls@canterbury.ac.nz>
Date:   Thu Aug 9 09:43:53 2012 +1200

    MDL-33640 - change $templatesinitialized to an array; improve naming and automate use of template

    * $templatesinitialized is now an array, so that subsequent calls to initialise_filepicker which request different repositories will include those (and only those) templates which it requires but have not yet been included
    * The get_template method has also been renamed to get_upload_template (and the template to "uploadform_" followed by the repository type), since it only applies to upload forms
    * If a plugin provides a get_upload_template method, the template it returns will now automatically be used instead of the standard uploadform template when generating an upload form