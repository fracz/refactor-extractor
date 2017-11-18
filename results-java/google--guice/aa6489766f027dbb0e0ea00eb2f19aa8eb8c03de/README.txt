commit aa6489766f027dbb0e0ea00eb2f19aa8eb8c03de
Author: paulmarshall <paulmarshall@google.com>
Date:   Mon Sep 19 14:15:06 2016 -0700

    Rewrite RealMapBinder to use InternalFactory

    This CL is a performance refactor.

    This does change the SPI dependency tree, in a way I believe more accurately
    describes the dependencies of each of the maps provided. Previously the dependencies for all maps was just a dependency on the entrySetBinder, which contained providers for all of the values. The new implementation has dependencies directly on the Vs for Map<K, V> and the multimap versions (e.g. Map<K, Set<V>>), and dependencies on Provider<V> for Map<K, Provider<V>> and the mutimap versions (e.g. Map<K, Set<Provider<V>>>).

    This implementation follows the same pattern as OptionalBinder, notably
    * All the mutable state is moved into a new inner class named BindingSelection
    * Calculating keys + set names is delayed until configure()/initialization time
    * None of the factories need circular deps resolution

    Caliper benchmarks show an improvement in all four bindings provided by
    MapBinder:
    https://caliper.[].com/runs/280cbcdd-9853-46a6-9852-2d591fc8b848,bfa5c0d0-002a-4eaa-9984-d4ac459daf16#r:scenario.benchmarkSpec.methodName,run.label
    (command used: [] run --run_under=perflab -- experimental/users/lukes/benchmarks:InjectionBenchmark -b timeMultimapBinderMultimapBinding,timeMultimapBinderMultimapProviderBinding,timeMultimapBinderMapBinding,timeMultimapBinderMapProviderBinding -t 3 -r {head, with_change})

    This has been run on the tap train and no failures are believed to be
    associated with this CL:
    []

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=133630536