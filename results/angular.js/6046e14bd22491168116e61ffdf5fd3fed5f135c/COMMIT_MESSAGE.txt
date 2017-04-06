commit 6046e14bd22491168116e61ffdf5fd3fed5f135c
Author: Tobias Bosch <tbosch1009@gmail.com>
Date:   Thu Sep 4 17:50:26 2014 -0700

    refactor(ngModelController,formController): centralize and simplify logic

    The previous logic for async validation in
    `ngModelController` and `formController` was not maintainable:
    - control logic is in multiple parts, e.g. `ctrl.$setValidity`
      waits for end of promises and continuous the control flow
      for async validation
    - logic for updating the flags `ctrl.$error`, `ctrl.$pending`, `ctrl.$valid`
      is super complicated, especially in `formController`

    This refactoring makes the following changes:
    - simplify async validation: centralize control logic
      into one method in `ngModelController`:
      * remove counters `invalidCount` and `pendingCount`
      * use a flag `currentValidationRunId` to separate
        async validator runs from each other
      * use `$q.all` to determine when all async validators are done
    - centralize way how `ctrl.$modelValue` and `ctrl.$invalidModelValue`
      is updated
    - simplify `ngModelController/formCtrl.$setValidity` and merge
      `$$setPending/$$clearControlValidity/$$clearValidity/$$clearPending`
      into one method, that is used by `ngModelController` AND
      `formController`
      * remove diff calculation, always calculate the correct state anew,
        only cache the css classes that have been set to not
        trigger too many css animations.
      * remove fields from `ctrl.$error` that are valid and add private `ctrl.$$success`:
        allows to correctly separate states for valid, invalid, skipped and pending,
        especially transitively across parent forms.
    - fix bug in `ngModelController`:
      * only read out `input.validity.badInput`, but not
        `input.validity.typeMismatch`,
        to determine parser error: We still want our `email`
        validator to run event when the model is validated.
    - fix bugs in tests that were found as the logic is now consistent between
      `ngModelController` and `formController`

    BREAKING CHANGE:
    - `ctrl.$error` does no more contain entries for validators that were
      successful.
    - `ctrl.$setValidity` now differentiates between `true`, `false`,
      `undefined` and `null`, instead of previously only truthy vs falsy.

    Closes #8941