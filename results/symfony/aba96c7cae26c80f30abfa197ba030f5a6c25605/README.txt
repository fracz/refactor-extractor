commit aba96c7cae26c80f30abfa197ba030f5a6c25605
Merge: 8c320b0 8321127
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jan 8 19:17:41 2013 +0100

    Merge branch '2.1'

    * 2.1:
      [Console] Fix style escaping parsing
      [Console] Make style formatter matching less greedy to avoid having to escape when not needed
      [Bundle] [FrameworkBundle] fixed indentation in esi.xml services file.
      [Component] [Security] fixed PSR-2 coding violation in ClassUtilsTest class.
      [Form] Fixed EntityChoiceList when loading objects with negative integer IDs
      [TwigBundle] There is no CSS visibility of display, should be visible instead
      [Form] corrected source node for a Danish translation
      [DependencyInjection] fixed a bug where the strict flag on references were lost (closes #6607)
      [HttpFoundation] Check if required shell functions for `FileBinaryMimeTypeGuesser` are not disabled
      [CssSelector] added css selector with empty string
      [HttpFoundation] Docblock for Request::isXmlHttpRequest() now points to Wikipedia
      [DependencyInjection] refactored code to avoid logic duplication
      [Form] Deleted references in FormBuilder::getFormConfig() to improve performance
      [HttpFoundation] Update docblock for non-working method

    Conflicts:
            src/Symfony/Bundle/TwigBundle/Resources/views/Exception/trace.html.twig
            src/Symfony/Bundle/TwigBundle/Resources/views/Exception/traces.html.twig