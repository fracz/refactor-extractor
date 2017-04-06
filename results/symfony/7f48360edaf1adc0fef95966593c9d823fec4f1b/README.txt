commit 7f48360edaf1adc0fef95966593c9d823fec4f1b
Merge: 2419000 ced865d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Oct 27 08:40:42 2015 -0700

    Merge branch '2.7' into 2.8

    * 2.7:
      fixed YAML files missing quotes when a string starts with @
      [VarDumper] Fix anonymous class dumping
      [Routing] mark internal classes
      [Translation][Csv file] remove unnecessary statements, for better readability.
      [Form] remove validation of FormRegistry::getType as FormRegistry::hasType does not validate either