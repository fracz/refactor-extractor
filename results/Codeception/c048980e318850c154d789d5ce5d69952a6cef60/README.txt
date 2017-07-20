commit c048980e318850c154d789d5ce5d69952a6cef60
Author: Zaahid Bateson <zbateson@gmail.com>
Date:   Tue Apr 7 22:48:27 2015 -0700

    Refactoring to merge fillField/submitForm

    - submitForm refactored to use DomCrawler's Form object, and grab
      defualt values from it instead of filtering the crawler for
      fields manually.
    - getFormFor refactored so it creates its own crawler.  Needed so
      input fields in disabled fieldsets aren't submitted
    - Created getFormValuesFor to get an array of values as expected by
      submitForm
    - Created proceedSubmitForm which is called instead of the deleted
      submitFormWithButton
    - Refactored getFormUrl into getFormUrl, getAbsoluteUrlFor and
      mergeUrl methods
    - More docblock comments for protected/private methods
    - Minor cleanup