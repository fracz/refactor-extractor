commit 34c3667350566dc89fcdd01dd1005194b34a2cb1
Author: Eric Wendelin <eric@gradle.com>
Date:   Tue Jan 24 07:58:38 2017 -0700

    Draft parallel console implementation

    This allows Gradle to show multiple items in progress when attached
    to an interactive terminal.

    This is achieved by:

    First, breaking apart ConsoleBackedProgressRenderer into 3 filters:
    - ThrottlingOutputEventListener buffers OutputEvents and flushes
    them after a certain period of time or if the build has ended
    - BuildStatusRenderer maintains a Label that displays overall
    build progress (formerly the "status bar")
    - WorkInProgressRenderer maintains a BuildProgressArea and
    associates one branch of a ProgressOperations "tree" to a Label
    representing work in progress for multiple workers.

    Second, externalizing and enhancing concepts within AnsiConsole:
    - Cursor represents a position in the terminal, using a cartesian
    coordinate system with origin (0, 0) at the bottom left
    - MultiLineProgressArea is a TextArea implementation with
    addressible lines through Labels.
    - Style represents ANSI text colors and emphases.
    - Span is simply an association between a Style and String of
    text.
    - AnsiExecutor is an ANSI-aware text writer. It accepts Actions
    that may reposition the Cursor and write styled text

    Finally, logging improvements. Project evaluation logging was
    extracted from build progress logging. Build progress logs are
    formatted with a ProgressBar formatter and submitted through
    the same ProgressOperations mechanism. These ProgressOperations
    are selected by the BuildStatusRenderer and rendered separately
    from other ProgressOperations for now. In the future, we will
    have must stronger semantics around this using BuildOperations.

    Issue: gradle/gradle-private#649