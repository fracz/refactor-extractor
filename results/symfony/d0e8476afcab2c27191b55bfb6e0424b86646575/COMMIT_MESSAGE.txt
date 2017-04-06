commit d0e8476afcab2c27191b55bfb6e0424b86646575
Merge: 5a0157f f4a6359
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 27 11:45:09 2016 +0100

    Merge branch '3.2'

    * 3.2:
      [FrameworkBundle] Ignore AnnotationException exceptions in the AnnotationsCacheWarmer
      fixed @return when returning this or static
      override property constraints in child class
      removed unneeded comment
      [Console] improved code coverage of Command class
      [FrameworkBundle] Make TemplateController working without the Templating component
      [FrameworkBundle] Allow multiple transactions with the same name
      Only count on arrays or countables to avoid warnings in PHP 7.2