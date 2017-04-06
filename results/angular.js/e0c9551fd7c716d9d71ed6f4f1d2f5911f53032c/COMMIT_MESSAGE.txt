commit e0c9551fd7c716d9d71ed6f4f1d2f5911f53032c
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Thu Mar 8 15:03:24 2012 -0800

    refactor(forms): remove registerWidget and use event instead

    Each widget (ng-model directive) emits $newFormControl event instead of getting hold of parent form
    and calling form.registerWidget(this);