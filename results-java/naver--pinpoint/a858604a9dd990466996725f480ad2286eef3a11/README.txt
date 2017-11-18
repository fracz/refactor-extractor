commit a858604a9dd990466996725f480ad2286eef3a11
Author: Woonduk Kang <wd.kang@navercorp.com>
Date:   Thu Jul 23 16:21:01 2015 +0900

    #714  trace api refactoring
     - remove unused class
       StackFrame, RootStackFrame, SpanEventStackFrame
       replaced SpanRecoder, SpanEventRecoder
     - added FrameAttachment