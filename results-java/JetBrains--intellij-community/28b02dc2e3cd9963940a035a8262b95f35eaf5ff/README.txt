commit 28b02dc2e3cd9963940a035a8262b95f35eaf5ff
Author: Sergey Karashevich <sergey.karashevich@jetbrains.com>
Date:   Sat Jul 29 11:49:23 2017 +0300

    [gui-test] improve ExtendedCellReaders

    - Add ExtendedJComboBoxCellReader.
    - Unite all cellreaders into one ExtendedCellReaders.kt.
    - Move Generators getText() to ExtendedCellReaders#valueAt()