commit 5db36518ed9a1cf6c0ff0fc0aa1aadfa57c4ac1a
Author: jawher <jawher.moussa@gmail.com>
Date:   Wed Nov 25 17:26:38 2015 +0100

    Include responses from a method's declared exceptions

    A common pattern in JAX-RS applications is to have resources throw
    custom exceptions, and configure an ExceptionMapper to map them to a
    specific HTTP status code.

    This pattern does not mesh well with how swagger collects the APIs
    info: swagger expects the developer to declare for every method the
    list of possible responses, regardless of the exceptions it declares to
    throw.

    ```java
    @PUT
    @ApiOperation("desc")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Everything is awesome")
            @ApiResponse(code = 500, message = "Internal error")
            @ApiResponse(code = 502, message = "Upstream error")
    })
    @Path("/op")
    void operation(Params params) throws UpstreamServerError, InternalServerError;
    ```

    This pull request proposes to also check the exceptions declared in the
    throws clause of a method for `@ApiResponse` annotations, and include
    them in the method's possible responses.

    So that the previous example would become:

    ```java
     @PUT
     @ApiOperation("desc")
     @ApiResponses({
            @ApiResponse(code = 204, message = "Everything is awesome")
     })
     @Path("/op")
     void operation(Params params) throws UpstreamServerError, InternalServerError;
     ```

    By moving the other response codes to the exceptions:

    ```java
    @ApiResponses({
           @ApiResponse(code = 502, message = "Upstream error")
    }
    public class UpstreamServerError extends RuntimeException {
      :
    }
    ```

    In my particular case, I have an app with hundreds of resource methods
    which already declare the possible exceptions they throw.
    This improvement would save me hundreds and hundreds of repetitions of
    return statuses.