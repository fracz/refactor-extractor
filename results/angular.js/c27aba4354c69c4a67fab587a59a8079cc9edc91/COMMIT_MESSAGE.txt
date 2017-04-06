commit c27aba4354c69c4a67fab587a59a8079cc9edc91
Author: Misko Hevery <misko@hevery.com>
Date:   Thu Nov 3 21:14:04 2011 -0700

    refactor(api): remove type augmentation

    BREAK:
      - remove angular.[Object/Array/String/Function]
      - in templates [].$filter(predicate) and friends need to change to [] | filter:predicate