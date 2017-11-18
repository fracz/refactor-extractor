commit 135aa0e516219c6e63df634aa72377d13eec4970
Author: lukes <lukes@google.com>
Date:   Tue Feb 21 12:59:44 2017 -0800

    Change InjectorImpl.callInContext to InjectorImpl.enterContext to reduce stack
    depth on common injection paths.

    This should save 2 stack frames per 'top level injection' and also we no longer need to allocate the ContextualCallable object.

    In a simple benchmark that injects a chain of 5 @Provides bindings and 5 ConstructorInjector bindings I saw the following improvements.

    Before:      time ns | objects
    provides     319.193 | 23
    constructor  322.787 | 23

    After:       time ns | objects
    provides     292.654 | 22
    constructor  295.167 | 22

    So ~a 8% improvement in provisioning time and one fewer allocation.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=148132739