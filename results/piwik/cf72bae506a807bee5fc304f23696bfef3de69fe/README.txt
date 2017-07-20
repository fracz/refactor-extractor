commit cf72bae506a807bee5fc304f23696bfef3de69fe
Author: sgiehl <stefan@piwik.org>
Date:   Sat Aug 31 18:11:39 2013 +0200

    completely refactored TranslationWriter to Translate\Writer
    - it's now able to filter/clean translations, which shouldn't be done in tests only
    - uses the new Translate\Filter and Translate\Validate classes
    - is now able to write the files, and not only return data and path to write