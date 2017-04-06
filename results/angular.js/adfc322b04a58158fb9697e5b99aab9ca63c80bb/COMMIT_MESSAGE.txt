commit adfc322b04a58158fb9697e5b99aab9ca63c80bb
Author: Shahar Talmi <shahar.talmi@gmail.com>
Date:   Fri May 9 02:20:19 2014 +0300

    refactor(ngModelOptions): move debounce and updateOn logic into NgModelController

    Move responsibility for pending and debouncing model updates into `NgModelController`.
    Now input directives are only responsible for capturing changes to the input element's
    value and then calling `$setViewValue` with the new value.

    Calls to `$setViewValue(value)` change the `$viewValue` property but these changes are
    not committed to the `$modelValue` until an `updateOn` trigger occurs (and any related
    `debounce` has resolved).

    The `$$lastCommittedViewValue` is now stored when `$setViewValue(value)` updates
    the `$viewValue`, which allows the view to be "reset" by calling `$rollbackViewValue()`.

    The new `$commitViewValue()` method allows developers to force the `$viewValue` to be
    committed through to the `$modelValue` immediately, ignoring `updateOn` triggers and
    `debounce` delays.

    BREAKING CHANGE:

    This commit changes the API on `NgModelController`, both semantically and
    in terms of adding and renaming methods.

    * `$setViewValue(value)` -
    This method still changes the `$viewValue` but does not immediately commit this
    change through to the `$modelValue` as it did previously.
    Now the value is committed only when a trigger specified in an associated
    `ngModelOptions` directive occurs. If `ngModelOptions` also has a `debounce` delay
    specified for the trigger then the change will also be debounced before being
    committed.
    In most cases this should not have a significant impact on how `NgModelController`
    is used: If `updateOn` includes `default` then `$setViewValue` will trigger
    a (potentially debounced) commit immediately.
    * `$cancelUpdate()` - is renamed to `$rollbackViewValue()` and has the same meaning,
    which is to revert the current `$viewValue` back to the `$lastCommittedViewValue`,
    to cancel any pending debounced updates and to re-render the input.

    To migrate code that used `$cancelUpdate()` follow the example below:

    Before:

    ```
      $scope.resetWithCancel = function (e) {
        if (e.keyCode == 27) {
          $scope.myForm.myInput1.$cancelUpdate();
          $scope.myValue = '';
        }
      };
    ```

    After:

    ```
      $scope.resetWithCancel = function (e) {
        if (e.keyCode == 27) {
          $scope.myForm.myInput1.$rollbackViewValue();
          $scope.myValue = '';
        }
      }
    ```