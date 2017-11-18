commit 89c8747df70a7a25af1623d71057db50ba841248
Author: tbreisacher <tbreisacher@google.com>
Date:   Tue Apr 18 16:48:01 2017 -0700

    Hoist variables declared with 'var' to the scope they're actually declared in, if they are referenced outside the block where the var statement is. For instance

      if (someCondition) {
        var a = 1;
      }
      var b = a || 2;

    becomes

      var a;
      if (someCondition) {
        a = 1;
      }
      var b = a || 2;

    Then, undo this hoisting in Denormalize so that it doesn't cause other optimizations to back off. This happens regardless of whether the hoisting described above actually happens, so this CL may improve code size for some projects.

    (Rolling forward again!)

    Fixes https://github.com/google/closure-compiler/issues/2388
    Fixes https://github.com/google/closure-compiler/issues/2364

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=153529451