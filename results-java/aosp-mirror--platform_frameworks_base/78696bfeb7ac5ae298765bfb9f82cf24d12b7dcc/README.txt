commit 78696bfeb7ac5ae298765bfb9f82cf24d12b7dcc
Author: Felipe Leme <felipeal@google.com>
Date:   Fri Mar 31 08:23:06 2017 -0700

    Moar pre-partitioning refactoring.

    On ViewState: split value into mCurrentValue and mAutofilledValue.
    On Session: replacing mAutofilledDataset by mDatasetWaitingAuth and
                ViewState.getAutofilledValue() (mAutofilledDataset is still needed,
                but will be removed in the first partitioning CL).

    Also fixed a missed 'return' on TimePicker.autofill()

    Bug: 35707731
    Test: CtsAutoFillServiceTestCases pass

    Change-Id: Icc32701ae3e499a77d99e6ae1daa7d070a3df631