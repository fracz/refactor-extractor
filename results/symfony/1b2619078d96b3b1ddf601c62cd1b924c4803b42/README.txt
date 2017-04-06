commit 1b2619078d96b3b1ddf601c62cd1b924c4803b42
Merge: d060c40 fd58a5f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 17 07:31:37 2013 +0200

    Merge branch '2.2'

    * 2.2:
      Fix default value handling for multi-value options
      [HttpKernel] truncate profiler token to 6 chars (see #7665)
      Disabled APC on Travis for PHP 5.5+ as it is not available
      [HttpFoundation] do not use server variable PATH_INFO because it is already decoded and thus symfony is fragile to double encoding of the path
      Fix download over SSL using IE < 8 and binary file response
      [Console] Fix merging of application definition, fixes #7068, replaces #7158
      [HttpKernel] fixed the Kernel when the ClassLoader component is not available (closes #7406)
      fixed output of bag values
      [Yaml] improved boolean naming ($notEOF -> !$EOF)
      [Yaml] fixed handling an empty value
      [Routing][XML Loader] Add a possibility to set a default value to null
      [Console] fixed handling of "0" input on ask
      The /e modifier for preg_replace() is deprecated in PHP 5.5; replace with preg_replace_callback()
      fixed handling of "0" input on ask
      [HttpFoundation] Fixed bug in key searching for NamespacedAttributeBag
      [Form] DateTimeToRfc3339Transformer use proper transformation exteption in reverse transformation
      Update PhpEngine.php
      [PropertyAccess] Add objectives to pluralMap
      [Security] Removed unused var
      [HttpFoundation] getClientIp is fixed.

    Conflicts:
            src/Symfony/Component/Console/Tests/Command/CommandTest.php
            src/Symfony/Component/Console/Tests/Input/ArgvInputTest.php
            src/Symfony/Component/HttpFoundation/Request.php
            src/Symfony/Component/HttpKernel/Kernel.php