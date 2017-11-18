commit d2e851cb750e29fd1382ec407502c01206cea2a7
Author: LYK <dalinaum@gmail.com>
Date:   Mon Aug 8 18:46:28 2016 +0900

    Add RealmQuery.in() (#3133)

    * Add RealmQuery.equalToAny() (#841)

    * PR feedback: equalToAny -> in

    * PR feedback: apply 'beginGroup()' and 'endGroup()' on 'in' operator.

    @zaki50: We should use beginGroup() and endGroup() since equalTo("a")
    and in("b", "c", "d") does not equal to equalTo("a") and equalTo("b") or
    equalTo("c") or equalTo("d").

    * PR feedback: throws an IllegalArgumentException if values is an empty array.

    * Add a test, in_date()

    * Remove unnecessary tests.

    * Fix type fields in in_byte()

    * Add 3 tests, in_byte(), in_short() and in_int()

    * Update CHANGELOG.md for RealmQuery.in()

    * PR feedback: array version -> vargs version.

    * PR feedback: ++i -> i++

    * PR feedback: fix javadocs.

    * PR feedback: remove unnecessary parenthesises.

    * PR feedback: revert the variable arguments version.

    * PR feedback: fix javadocs and improve error messages.

    * PR feedback: Extract the constant string literal from the hard-coded error message.

    * PR feedback: Adopt boxed type API.

    * PR feedback: add null tests.

    * Fix row_isValid

    * Fix openPreNullWithRequired

    * PR feedback: update javadocs.

    * PR feedback: use NoPrimaryKeyNullTypes for testing instead of AllTypes.

    * PR feedback: Revert AllTypes.

    * Revert automatic formatting.

    * Revert auto-formatting again.

    * PR feedback: remove unnecessay empty lines.

    * PR feedback: generalize tests.

    * PR feedback: update javadocs.