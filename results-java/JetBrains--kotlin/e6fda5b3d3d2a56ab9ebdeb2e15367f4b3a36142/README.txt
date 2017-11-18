commit e6fda5b3d3d2a56ab9ebdeb2e15367f4b3a36142
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Thu Mar 29 19:47:50 2012 +0400

    minor codegen refactoring

    * cleanup after yesterday
    * remove BindingContext stack in GenerationState
    * use more power and strength of di

    TODO: also initialize GenerationState by DI