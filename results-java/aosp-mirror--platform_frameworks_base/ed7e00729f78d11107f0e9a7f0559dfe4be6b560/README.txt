commit ed7e00729f78d11107f0e9a7f0559dfe4be6b560
Author: Brian Carlstrom <bdc@google.com>
Date:   Thu Mar 24 13:27:57 2011 -0700

    SamplingProfilerIntegration and SamplingProfiler improvements (3/3)

    Summary:

    frameworks/base
    - fix profiling to collect data beyond the first snapshot
    - avoid many small files, accumulate data over process lifetime

    libcore:
    - add support for VM specific sampling, trying to cut down overhead
    - added support for converting snapshot files to text format
    - fixed race in profiler when stopping

    dalvik
    - added VMStack.setThreadStackTrace interface for filling a stack
      trace into an existing StackTraceElement[]

    Details:

    frameworks/base

        Changed snapshots from text to binary hprof format (bumping version to 3)
        Changed from one file per snapshot to one file per process lifetime.
        Restart profiling after snapshot.

            core/java/com/android/internal/os/SamplingProfilerIntegration.java

        Add quick test in maybeSnapshot to avoid doing work when the
        SamplingProfilerIntegration is disabled. Make maybeSnapshot
        private. Remove unneeded memory allocation in handleLowMemory.

            core/java/android/app/ActivityThread.java

    libcore

        Added ThreadSampler interface. This allows VM specific thread
        sampling optimizations. The portable version continues to use
        Thread.getStackTrace().

            dalvik/src/main/java/dalvik/system/profiler/ThreadSampler.java
            dalvik/src/main/java/dalvik/system/profiler/PortableThreadSampler.java
            dalvik/src/main/java/dalvik/system/profiler/SamplingProfiler.java

        Add VMStack.setThreadStackTrace and use in new DalvikThreadSampler
        to avoid allocating a full stack trace when only a limited depth
        is desired.

            dalvik/src/main/java/dalvik/system/profiler/DalvikThreadSampler.java
            dalvik/src/main/java/dalvik/system/VMStack.java

        Refactored BinaryHprof.readMagic out of BinaryHprofReader so it
        can be used by HprofBinaryToAscii converter to probing file
        types. Added magic number constant to be shared between readMagic
        and BinaryHprofWriter.

            dalvik/src/main/java/dalvik/system/profiler/BinaryHprof.java
            dalvik/src/main/java/dalvik/system/profiler/BinaryHprofReader.java
            dalvik/src/main/java/dalvik/system/profiler/BinaryHprofWriter.java
            dalvik/src/main/java/dalvik/system/profiler/HprofBinaryToAscii.java

        Removed unneeded HprofWriter interface. Changed to simpler static
        interface to write HprofData to binary and text formats.

            dalvik/src/main/java/dalvik/system/profiler/HprofWriter.java
            dalvik/src/main/java/dalvik/system/profiler/AsciiHprofWriter.java
            dalvik/src/main/java/dalvik/system/profiler/BinaryHprofWriter.java
            dalvik/src/test/java/dalvik/system/profiler/SamplingProfilerTest.java

        Added support for reading snapshot files created by
        SamplingProfilerIntegration by stripping the text header to allow
        easier conversion to the text format.

            dalvik/src/main/java/dalvik/system/profiler/HprofBinaryToAscii.java

        Fixed race between Sampler and
        SamplingProfiler.stop. SamplingProfiler.stop previously simply
        called the Sampler's TimerTask.cancel method, but this does not
        wait for a currently running Sampler to finish. The TimerTask
        documentation says the only reliable way to do this is to have the
        run() cancel itself, so that is what is now done, with new code to
        ensure that SamplingProfiler.stop does not return until the
        Sampler has been terminated.

            dalvik/src/main/java/dalvik/system/profiler/SamplingProfiler.java

    dalvik

       Refactored VMStack_getThreadStackTrace to create helper getTraceBuf
       used to implement new VMStack_setThreadStackTrace. The new version
       interface fills an existing StackTraceElement[], avoid allocating
       unnecessary StackTraceElements.

            vm/native/dalvik_system_VMStack.c

       Refactor dvmGetStackTraceRaw to create dvmSetStackTraceRaw which
       fills in an existing, potentially smaller, StackTraceElement[].

            vm/Exception.c
            vm/Exception.h

       Change stack depths to be unsigned to avoid signed/unsigned comparison warnings.

            vm/Ddm.c
            vm/Exception.c
            vm/Exception.h

    Change-Id: I4b90255e4e1d33ea2b569321c4968b0f3369f251