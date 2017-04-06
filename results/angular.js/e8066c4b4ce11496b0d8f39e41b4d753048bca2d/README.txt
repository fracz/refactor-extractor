commit e8066c4b4ce11496b0d8f39e41b4d753048bca2d
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Wed Dec 11 15:54:37 2013 -0500

    feat($compile): explicitly request multi-element directive behaviour

    Directives which expect to make use of the multi-element grouping feature introduced in
    1.1.6 (https://github.com/angular/angular.js/commit/e46100f7) must now add the property multiElement
    to their definition object, with a truthy value.

    This enables the use of directive attributes ending with the words '-start' and '-end' for
    single-element directives.

    BREAKING CHANGE: Directives which previously depended on the implicit grouping between
    directive-start and directive-end attributes must be refactored in order to see this same behaviour.

    Before:

    ```
    <div data-fancy-directive-start>{{start}}</div>
      <p>Grouped content</p>
    <div data-fancy-directive-end>{{end}}</div>

    .directive('fancyDirective', function() {
      return {
        link: angular.noop
      };
    })
    ```

    After:

    ```
    <div data-fancy-directive-start>{{start}}</div>
      <p>Grouped content</p>
    <div data-fancy-directive-end>{{end}}</div>

    .directive('fancyDirective', function() {
      return {
        multiElement: true, // Explicitly mark as a multi-element directive.
        link: angular.noop
      };
    })
    ```

    Closes #5372
    Closes #6574
    Closes #5370
    Closes #8044
    Closes #7336