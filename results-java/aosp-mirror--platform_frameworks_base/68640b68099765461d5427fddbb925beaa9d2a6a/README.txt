commit 68640b68099765461d5427fddbb925beaa9d2a6a
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Thu Jun 2 14:56:18 2016 +0100

    Add setOnDateChangedListener to DatePicker

    Also refactor slightly the hierarchy in TimePicker to use the
    common parent AbstractTimePickerDelegate in the same way as
    DatePicker.

    Bug: 28310875
    Change-Id: Iecaf206ba1691e26d4496014dc1d13f070c4722a