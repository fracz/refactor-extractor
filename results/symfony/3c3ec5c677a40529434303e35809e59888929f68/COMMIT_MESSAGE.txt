commit 3c3ec5c677a40529434303e35809e59888929f68
Merge: ff9b731 039ff6f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 11 12:08:10 2012 +0200

    merged branch tvlooy/GetSetMethodNormalizer (PR #3582)

    Commits
    -------

    039ff6f allow more control on GetSetMethodNormalizer by using callback functions and an ignoreAttributes list

    Discussion
    ----------

    allow more control on GetSetMethodNormalizer

    Here is an other attempt. You would use this as follows:

        $serializer = new \Symfony\Component\Serializer\Serializer(
            array(new \Symfony\Component\Serializer\Normalizer\GetSetMethodNormalizer()),
            array('json' => new \Symfony\Component\Serializer\Encoder\JsonEncoder())
        );

        $callbacks = array('books' => function ($books) { return NULL; });

        return new Response(
            $serializer->serialize($paginator->getRows(), 'json', $callbacks),
            200,
            array('Content-Type' => 'application/json')
        );

    Besides of returning NULL, you could also do things like:

        $callbacks = array(
            'books' => function ($books) {
                $ids = array();
                foreach ($books as $book) {
                    $ids[] = $book->getId();
                }
                return $ids;
            },
            'author' => function ($author) {
                return $author->getId();
            },
            'creationDate' => function ($creationDate) {
                return $creationDate->format('d/m/Y');
            },
        );

    The commit is not complete yet. But at this point I am interested in your opinions.

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-03-12T22:53:18Z

    in general i agree that using a callback is a good solution to provide more power without complicating the API or implementation in this case.

    please add a test case, this should also help illustrate how this can be used in practice.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-03-13T04:54:33Z

    Note that your change breaks the API defined by the interface, i.e. someone using this method needs to type-hint the serializer implementation, not the interface.

    It also adds a parameter to the public API of the serializer which will only work with one specific normalizer. What if another normalizer needs additional information, should another parameter be added to the serialize method? What about deserialization?

    Bottom line is, the serializer component was simply not designed for this kind of thing. I've tried to make it more flexible before creating the bundle, but some things simply cannot be fixed in a sane way.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-13T06:07:45Z

    Would just adding a setCallbacks() to the GetSetMethodNormalizer be a better solution? That doesn't touch the API. I will try to write some tests this evening.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-03-13T16:22:50Z

    That would definitely be better.

    You would then need to retrieve the normalizer instance before calling ``serialize`` on the serializer which also leaves a stale taste, but I have no other solution for now.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-13T21:32:26Z

    So, this should be it then. Yet an other usage example:

        $normalizer = new \Symfony\Component\Serializer\Normalizer\GetSetMethodNormalizer();
        $normalizer->setCallbacks(
            array(
                'books' => function ($books) {
                    $ids = array();
                    foreach ($books as $book) {
                        $ids[] = $book->getId();
                    }
                    return $ids;
                },
            )
        );
        $serializer = new \Symfony\Component\Serializer\Serializer(
            array($normalizer),
            array('json' => new \Symfony\Component\Serializer\Encoder\JsonEncoder())
        );

        return new Response(
            $serializer->serialize($paginator->getRows(), 'json'),
            200,
            array('Content-Type' => 'application/json')
        );

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-18T21:16:48Z

    Anything else needed for this to get pulled in?

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-19T18:33:58Z

    Hm, I like to keep it that way because I like the fact that not passing a callable will result in a warning instead of silently skipping it. You don't get that behaviour by treating it as null.

    ---------------------------------------------------------------------------

    by vicb at 2012-03-19T23:15:37Z

    I was unclear: the code should throw an exception when an element is not callable, this is why `null` will not be supported any more (it is not a callback as the `setCallbacks` indicate).

    They are several way to support the former behavior:

    * the cb can return a defined interface,
    * the cb can throw a defines exc,
    * by adding a `setIgnoredAttributes` method

    Please also squash your commits.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-20T21:02:06Z

    Yes, I like the setIgnoredAttributes solution. I changed it and squashed the commits.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-03-26T20:07:36Z

    some improvements and squashed the commits

    ---------------------------------------------------------------------------

    by stof at 2012-04-03T22:36:15Z

    @tvlooy Please rebase your branch. It conflicts with master because of the move of tests.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-04-04T07:43:47Z

    @stof I will do it on saturday, if that is ok with you.

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-10T18:29:30Z

    Is it mergeable now? ping @Seldaek, @schmittjoh.

    ---------------------------------------------------------------------------

    by tvlooy at 2012-04-10T18:55:04Z

    yes, it should be