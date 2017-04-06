commit 4982984d31e0267000e257c0733c3909686f7c5f
Merge: add32ce 14c417a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 3 04:55:50 2014 +0100

    Merge branch '2.5'

    * 2.5:
      added missing files
      [TwigBundle] added a test
      Indicate which file was being parsed if an exception is thrown while running translation:debug
      [ClassLoader] Cast $useIncludePath property to boolean
      [HttpFoundation] Minor spelling fix in PHPDocs
      improve error message for multiple documents
      Remove aligned '=>' and '='
      [Session] remove invalid workaround in session regenerate
      [Kernel] ensure session is saved before sending response
      [Routing] serialize the compiled route to speed things up
      [Form] Fixed usage of "name" variable in form_start block
      [Validator] Fixed Regex::getHtmlPattern() to work with complex and negated patterns
      [DependencyInjection] use inheritdoc for loaders
      [Config] fix filelocator with empty name
      [Form] fix form handling with unconventional request methods like OPTIONS
      CSRF warning docs on Request::enableHttpMethodParameterOverride()

    Conflicts:
            src/Symfony/Component/Console/Helper/ProgressBar.php