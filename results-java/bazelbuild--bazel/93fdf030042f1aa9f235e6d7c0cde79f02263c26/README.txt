commit 93fdf030042f1aa9f235e6d7c0cde79f02263c26
Author: tomlu <tomlu@google.com>
Date:   Thu Aug 24 02:33:57 2017 +0200

    Restore VectorArg to CustomCommandLine's public interface.

    This CL intends to remove 81 overloads from CustomCommandLine, replacing them with 6 overloads that operate on VectorArg. The actual inlining isn't done in this CL to make it easier to review.

    This CL tries to balance reducing the API contact surface area while retaining the readability and discoverability of these methods, ensuring that they will be used over methods that eagerly convert arguments to strings.

    PiperOrigin-RevId: 166280990