commit bbd686a6858e73ac7d4cd7ca5a87b159ce89147a
Merge: c4df572 5fa1c70
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 15 15:42:36 2012 +0100

    merged branch igorw/json-response (PR #3375)

    Commits
    -------

    5fa1c70 [json-response] Add a JsonResponse class for convenient JSON encoding

    Discussion
    ----------

    [json-response] Add a JsonResponse class for convenient JSON encoding

    Usage example:

        $data = array(user => $user->toArray());
        return new JsonResponse($data);

    ---------------------------------------------------------------------------

    by drak at 2012-02-16T11:51:11Z

    @fabpot - maybe we could benefit with a bit more sub-namespacing in this component.  One for Response for example and probably one for Request.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-02-16T15:07:31Z

    @drak Please no. Moving the session was already a pain IMO since it was type-hinted in a few places (lack of interface, and interface doesn't include flash stuff still). Creating BC breaks just for fun like that is annoying for interop of bundles. It doesn't matter whether we have 10 or 15 classes in one directory.

    ---------------------------------------------------------------------------

    by drak at 2012-02-17T08:33:46Z

    @francodacosta The most optimal place is `__toString()`.

    @Saldaek It just looks like the whole namespace is getting more cluttered.  I suggest it because things like Request/Response objects are surely only going to grow over time.  There is always the possibility to make BC for moved and renamed classes so there doesn't have to be any extra complications for making things look cleaner. Anyway, just a thought :-)

    ---------------------------------------------------------------------------

    by stof at 2012-02-17T14:47:40Z

    @drak Changing the namespace of a class is a BC break. The request and the response are used in many more places than the Session so it would be a real pain to update this. And the component is tagged with ``@api`` so BC breaks are forbidden without a good reason. The session refactoring was one as it was really an issue in the implementation, but simply renaming the class is not.

    ---------------------------------------------------------------------------

    by fabpot at 2012-03-05T15:03:53Z

    I'm -1 for adding this to the core. It does not add much value and why add a special response for JSON and not other formats?

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-05T18:38:05Z

    I think it's useful because it's a class we need in almost every project, and I don't think we're alone. It's super simple but makes me wonder every time why I have to recreate it. I don't want an additional bundle just for 3lines of code. Similarly I would say a JsonpResponse would be great, or maybe just an optional $callback arg to the json response to enable jsonp mode.

    I just had someone ask me on irc how to do JSONP so while I think it's obvious and I'm sure you'd think that too, it obviously isn't to newcomers. The Response stuff is hidden behind those render methods & such and people don't realize they can simply subclass. If a few examples were in core it would be both helpful for learning and useful on a day to day basis.

    As for other formats, well JSON is typically used nowadays, except when you want more fancy XML APIs, but for that the JMSSerializerBundle + FOSRestBundle are superior and we can't achieve such things in a few lines of code. I could also see a BinaryResponse or DownloadResponse or such that has proper "force-download" headers and accepts any binary stream, but that's another debate.

    ---------------------------------------------------------------------------

    by dragoonis at 2012-03-05T19:43:05Z

    I'm +1 for the concept but not commenting on how it should be implemented I'll leave that to other people.

    Typically when you want to force a download you have to do ``content-disposition: attachment; filename="filehere.pdf"``
    Modifying some response headers and the likes automatically for the user by returning a DownloadResponse object would be very handy..

    I'm +1 for @Seldaek's point about examples of sub-classing for specific use cases. It will help with demonstrating how to do custom stuff the right way rather than people coming up with their own contraptions.

    ---------------------------------------------------------------------------

    by stof at 2012-03-05T20:14:39Z

    btw, regarding the BinaryResponse, there is a pending PR about it: #2606

    ---------------------------------------------------------------------------

    by simensen at 2012-03-05T21:07:33Z

    I'm +1 for providing reference implementations fo custom Response cases. I wanted to find best practices for handling JSONP requests/responses and couldn't find anything at all on the topic. I thought maybe extending Response might be useful but wasn't sure if that could be done safely or should be done at all.

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-03-05T22:28:01Z

    @stof i think @drak was suggesting moving the class, but leaving an empty class extending from the new class in the old location to maintain BC

    ---------------------------------------------------------------------------

    by stof at 2012-03-05T23:55:36Z

    @lsmith77 This would force Symfony to use the BC class so that it does not break all typehints in existing code

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-03-06T00:22:15Z

    BC hacks are never nice .. the goal would just be to eventually have all those classes and more importantly all new ones in a subnamespace. actually it might be easier to just leave all the classes in the old location and create new ones extending from the old ones. anyway .. personally i am also not such a big fan of these specialized responses .. but i guess i see FOSRestBundle as the alternative answer which makes me biased.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-06T07:57:36Z

    I'm using FOSRestBundle when it's needed, but when you just have a small scale app that needs one or two json responses for specialized stuff it is slightly overkill. And again, newcomers probably won't know about it, and encouraging using it for simple use cases isn't exactly the best learning curve we can provide.

    ---------------------------------------------------------------------------

    by COil at 2012-03-06T23:12:15Z

    +1 for this. I have implemented such a function in all my sf1 projects, it will be the same for sf2.

    ---------------------------------------------------------------------------

    by fabpot at 2012-03-15T13:22:27Z

    Closing this PR in favor of a cookbook that explains how a developer can override the default Response class (this JSON class being a good example). see symfony/symfony-docs#1159

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-15T13:25:08Z

    Meh. Forcing people to copy paste code from the cookbook in every second project isn't exactly a step forward with regard to ease of use and user-friendliness.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-15T13:26:48Z

    I mean following this logic, things like the X509 authentication should just be put in cookbooks too because almost nobody needs that. We have tons of code in the framework, I don't get the resistance with adding such a simple class which makes code more expressive.

    ---------------------------------------------------------------------------

    by fabpot at 2012-03-15T13:53:07Z

    because X509 authentication is not easy to get it right. Sending a JSON response is as simple as it can get:

        new Response(json_encode($data), 200, array('Content-Type' => 'application/json'));

    ---------------------------------------------------------------------------

    by marijn at 2012-03-15T13:54:25Z

    Perhaps we need a `Symfony\Extensions\{Component}` namespace for things that don't necessarily belong in the core but are truly useful...

    ---------------------------------------------------------------------------

    by Seldaek at 2012-03-15T14:03:40Z

    I still fail to see why it doesn't belong in core.. There are tons of little helpers here and there, a base controller class made only of proxies, and then this gets turned down because it is simple to do it yourself? Sure it is simple, but it's repetitive and boring too. And while it's simple when you know your way around, some people aren't really sure how to do it.

    The whole point of a framework is to avoid repetitive bullshit and be more productive. @fabpot do you have any real arguments against? I can see that you don't see a big use to it, fair enough, but do you see any downside at all?