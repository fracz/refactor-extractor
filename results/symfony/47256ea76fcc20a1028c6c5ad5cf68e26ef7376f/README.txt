commit 47256ea76fcc20a1028c6c5ad5cf68e26ef7376f
Author: Jeremy Mikola <jmikola@gmail.com>
Date:   Sun Dec 4 16:09:11 2011 -0800

    [DependencyInjection] Make exceptions consistent when ContainerBuilder is frozen

    Some methods previously threw LogicExceptions when the ContainerBuilder was frozen. Using BadMethodCallException (a descendant of LogicException) in all cases improves consistency and preserves BC.