commit 3ad55eb83915259dc9faef1a379ccd39fb35ec6f
Author: Xiaoyan Lin <linxiaoyan18@gmail.com>
Date:   Mon Mar 21 18:06:05 2016 -0700

    Speed up the slow path of FastThreadLocal

    Motivation:

    The current slow path of FastThreadLocal is much slower than JDK ThreadLocal. See #4418

    Modifications:

    - Add FastThreadLocalSlowPathBenchmark for the flow path of FastThreadLocal
    - Add final to speed up the slow path of FastThreadLocal

    Result:

    The slow path of FastThreadLocal is improved.