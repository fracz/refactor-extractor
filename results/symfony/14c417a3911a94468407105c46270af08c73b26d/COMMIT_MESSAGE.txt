commit 14c417a3911a94468407105c46270af08c73b26d
Merge: b2ddfa8 a08fda5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 3 04:54:42 2014 +0100

    Merge branch '2.3' into 2.5

    * 2.3:
      added missing files
      [TwigBundle] added a test
      Indicate which file was being parsed if an exception is thrown while running translation:debug
      [ClassLoader] Cast $useIncludePath property to boolean
      [HttpFoundation] Minor spelling fix in PHPDocs
      improve error message for multiple documents
      [Session] remove invalid workaround in session regenerate
      [Kernel] ensure session is saved before sending response
      [Routing] serialize the compiled route to speed things up
      [Validator] Fixed Regex::getHtmlPattern() to work with complex and negated patterns
      [DependencyInjection] use inheritdoc for loaders
      [Config] fix filelocator with empty name
      [Form] fix form handling with unconventional request methods like OPTIONS
      CSRF warning docs on Request::enableHttpMethodParameterOverride()

    Conflicts:
            src/Symfony/Component/Routing/Route.php