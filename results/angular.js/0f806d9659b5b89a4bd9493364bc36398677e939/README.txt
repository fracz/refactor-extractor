commit 0f806d9659b5b89a4bd9493364bc36398677e939
Author: The Big Red Geek <rhyneandrew@gmail.com>
Date:   Fri Aug 29 22:03:13 2014 -0700

    refactor(ngSwitch): remove undocumented `change` attribute from ngSwitch

    BREAKING CHANGE:

    Ever since 0df93fd, tagged in v1.0.0rc1, the ngSwitch directive has had an undocumented `change`
    attribute, used for evaluating a scope expression when the switch value changes.

    While it's unlikely, applications which may be using this feature should work around the removal
    by adding a custom directive which will perform the eval instead. Directive controllers are
    re-instantiated when being transcluded, so by putting the attribute on each item that you want
    to be notified of a change to, you can more or less emulate the old behaviour.

    Example:

    ```js
    angular.module("switchChangeWorkaround", []).
      directive("onSwitchChanged", function() {
        return {
          linke: function($scope, $attrs) {
            $scope.$parent.$eval($attrs.change);
          }
        };
      });
    ```

    ```html
    <div ng-switch="switcher">
      <div ng-switch-when="a" on-switch-changed="doSomethingInParentScope()"></div>
      <div ng-switch-when="b" on-switch-changed="doSomethingInParentScope()"></div>
    </div>
    ```

    Closes #8858
    Closes #8822