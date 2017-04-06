commit d21584ec68497ca5402cb62695d32e8e4f5e334d
Merge: eb05fb0 aecc9b1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Oct 28 08:10:00 2012 +0100

    merged branch fabpot/prng (PR #4763)

    This PR was merged into the master branch.

    Commits
    -------

    aecc9b1 fixed tests when OpenSsl is not enabled in PHP, renamed a missnamed test, added missing license doc blocks
    ca567b5 fixed CS
    5cdf696 added a SecureRandomInterface
    234f725 rename String to StringUtils
    5849855 moved the secure random dep for remember me as a constructor argument
    248703f renamed Prng to SecureRandom
    c0c8972 simplified the Prng code
    e5dc7af moved the secure random class from JMSSecurityExtraBundle to Symfony (closes #3595)

    Discussion
    ----------

    [2.2][Security] Add a PRNG (closes #3595)

    As per #3595, I have moved the secure random class from JMSSecurityExtraBundle to Symfony.

    It has more impact than I expected ;)

    As you will see, the implementation has been refactored a bit. The most notable change is that Doctrine support has been moved to the bridge with the addition of a proper Doctrine seed provider (Doctrine is not a special case anymore).

    The Doctrine configuration has been moved to the DoctrineBundle: doctrine/DoctrineBundle#91

    schmittjoh/JMSSecurityExtraBundle#65 removes the code that has been moved.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-07-05T13:26:01Z

    I'm all for more security features, and both the String class & the Prng class for wrapping openssl make a lot of sense IMO, but I fail to see the use of the rest.

    If we just want a seed to have a fallback in case openssl is missing, I'd rather have a secret in the config.yml than a million classes to store the same secret in the DB. Maybe I'm missing something though? /cc @schmittjoh

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-05T16:32:10Z

    Having the configuration in different places (SecurityBundle & DoctrineBundle) feels a bit weird. I would prefer an approach similar to ACL, or the user provider/firewall section with factories. The latter being a bit more work to implement and the former potentially asking for complaints about too tight coupling to Doctrine.

    Regarding testing, we probably need to move the disableOpenSsl method to the SecureRandom class in order to allow OpenSSL to be disabled for testing and we also need to change the byte generation algorithm to produce the same output for the same starting seed. I agree that it does not make sense to introduce an interface for SecureRandom as only the seed providers should be replaced.

    As for the seed itself, it is constantly updated and does not stay the same as in the beginning. Thus, we need a provider that we can write to, and not only read from. I'm also not sure about using OpenSSL on Windows as I have read enough resources which claimed that the entropy on Windows is not always good (including OpenSSL docs). Always using the custom seed provider at least always ensured proper entropy even if OpenSSL's speed issues have been fixed in newer PHP versions.

    ---------------------------------------------------------------------------

    by stof at 2012-07-05T16:44:24Z

    @schmittjoh everything is in SecurityBundle now as it does not use a database anymore

    ---------------------------------------------------------------------------

    by stof at 2012-07-05T16:44:59Z

    and there is no seed provider anymore either

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-05T16:53:39Z

    Not having a seed provider is not such a good idea, but having a file-based seed provider is.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-07-05T17:01:18Z

    @schmittjoh why would you need to replace the seed provider? Don't you think that people serious about security to the point that they would want a stronger seed provider would enable openssl instead?

    ---------------------------------------------------------------------------

    by stof at 2012-07-05T17:06:50Z

    Well, what I meant is that there is no interchangeable provider anymore. The Prng class uses the file directly.

    And btw, I think the Prng class should be mockable for tests, so it should either have an interface or not be final (I vote for adding an interface)

    ---------------------------------------------------------------------------

    by jalliot at 2012-07-09T18:46:12Z

    @fabpot @schmittjoh What about using more fallbacks for `openssl_random_pseudo_bytes` like in @Seldaek's post ["Unpredictable hashes for humans"](http://seld.be/notes/unpredictable-hashes-for-humans)?
    Trying `mcrypt_create_iv` first might also be faster.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-07-10T08:52:46Z

    @jalliot I think mcrypt should be after if you make it use /dev/urandom, not 100% sure but openssl is probably higher quality than urandom.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-10T09:12:07Z

    The fallback algorithm that I added should be enough (it passes the
    statistical randomness tests).

    On Tue, Jul 10, 2012 at 10:52 AM, Jordi Boggiano <
    reply@reply.github.com
    > wrote:

    > @jalliot I think mcrypt should be after if you make it use /dev/urandom,
    > not 100% sure but openssl is probably higher quality than urandom.
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/4763#issuecomment-6870145
    >

    ---------------------------------------------------------------------------

    by stof at 2012-10-13T17:20:06Z

    @fabpot please send a PR to the doc so that this can be merged :smiley:

    ---------------------------------------------------------------------------

    by stof at 2012-10-13T17:22:08Z

    hmm, actually, some comments have not been taken into account yet so it is not ready to be merged

    ---------------------------------------------------------------------------

    by stof at 2012-10-27T07:14:43Z

    you forgot the SecureRandom file

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-27T08:49:54Z

    I think I've addressed all the comments. If everyone agree with the current implementation, I'm going to start updating the documentation.

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-27T10:51:15Z

    I've fixed the remaining CS issues.

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-28T07:00:31Z

    Documentation is here: symfony/symfony-docs#1858