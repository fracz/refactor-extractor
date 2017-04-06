commit ced865deb16c392d5acd8d7a0d7e0f8300949021
Merge: f042197 95ff0bc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Oct 27 08:38:06 2015 -0700

    Merge branch '2.3' into 2.7

    * 2.3:
      fixed YAML files missing quotes when a string starts with @
      [Routing] mark internal classes
      [Translation][Csv file] remove unnecessary statements, for better readability.
      [Form] remove validation of FormRegistry::getType as FormRegistry::hasType does not validate either