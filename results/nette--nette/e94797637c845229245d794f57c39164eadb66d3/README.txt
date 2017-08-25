commit e94797637c845229245d794f57c39164eadb66d3
Author: David Grudl <david@grudl.com>
Date:   Tue Dec 10 03:14:08 2013 +0100

    Forms: refactoring of SelectBox, MultiSelectBox and RadioList; they are ChoiceControl and MultiChoiceControl descendants

    BC break: MultiSelectBox is no more SelectBox descendant