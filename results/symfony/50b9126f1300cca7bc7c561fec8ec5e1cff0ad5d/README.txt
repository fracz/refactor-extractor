commit 50b9126f1300cca7bc7c561fec8ec5e1cff0ad5d
Merge: 9e66aaf 1f2521e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 2 12:20:21 2017 -0800

    feature #18193 [FrameworkBundle] Introduce autowirable ControllerTrait (dunglas)

    This PR was squashed before being merged into the 3.3-dev branch (closes #18193).

    Discussion
    ----------

    [FrameworkBundle] Introduce autowirable ControllerTrait

    | Q | A |
    | --- | --- |
    | Branch | master |
    | Bug fix? | no |
    | New feature? | yes |
    | BC breaks? | no |
    | Deprecations? | no |
    | Tests pass? | yes |
    | Fixed tickets | #16863 |
    | License | MIT |
    | Doc PR | todo |

    This is the missing part of the new controller system and it's fully BC with the old one.

    Used together with the existing autowiring system, #17608 and [DunglasActionBundle](https://github.com/dunglas/DunglasActionBundle) it permits to inject explicit dependencies in controllers with 0 line of config. It's a great DX improvement for Symfony.
    It also has a lot of another advantages including enabling to reuse controller accros frameworks and make them easier to test. See https://dunglas.fr/2016/01/dunglasactionbundle-symfony-controllers-redesigned/ for all arguments.

    Magic methods of old controllers are still available if you use this new trait in actions.

    For instance, the [`BlogController::newAction`](https://github.com/symfony/symfony-demo/blob/master/src/AppBundle/Controller/Admin/BlogController.php#L70) form the `symfony-demo` can now looks like:

    ``` php
    namespace AppBundle\Action\Admin;

    use AppBundle\Form\PostType;
    use AppBundle\Utils\Slugger;
    use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
    use Symfony\Bundle\FrameworkBundle\Controller\ControllerTrait;
    use Symfony\Component\HttpFoundation\Request;
    use Symfony\Component\Form\Extension\Core\Type\SubmitType;

    class NewAction {
        use ControllerTrait;

        private $slugger;

        public function __construct(Slugger $slugger)
        {
            $this->slugger = $slugger;
        }

        /**
         * @Route("/new", name="admin_post_new")
         */
        public function __invoke(Request $request)
        {
            $post = new Post();
            $post->setAuthorEmail($this->getUser()->getEmail());

            $form = $this->createForm(PostType::class, $post)->add('saveAndCreateNew', SubmitType::class);
            $form->handleRequest($request);

            if ($form->isSubmitted() && $form->isValid()) {
                $post->setSlug($this->slugger->slugify($post->getTitle()));
                $entityManager = $this->getDoctrine()->getManager();
                $entityManager->persist($post);
                $entityManager->flush();

                $this->addFlash('success', 'post.created_successfully');
                if ($form->get('saveAndCreateNew')->isClicked()) {
                    return $this->redirectToRoute('admin_post_new');
                }

                return $this->redirectToRoute('admin_post_index');
            }

            return $this->render('admin/blog/new.html.twig', array(
                'post' => $post,
                'form' => $form->createView(),
            ));
        }
    }
    ```

    As you can see, there is no need to register the `slugger` service in `services.yml` anymore and the dependency is explicitly injected. In fact the container is not injected in controllers anymore.

    Convenience methods still work if the `ControllerTrait` is used (of course it's not mandatory). Here I've made the choice to use an invokable class but this is 100% optional, a class can still contain several actions if wanted.

    Annotations like `@Route` still work too. The old `abstract` controller isn't deprecated. There is no valid reason to deprecate it IMO. People liking using the "old" way still can.

    Unless in #16863, there is only one trait. This trait/class is basically a bunch of proxy methods to help newcomers. If you want to use only some methods, or want explicit dependencies (better), just inject the service you need in the constructor and don't use the trait.

    I'll create open a PR on the standard edition soon to include ActionBundle and provide a dev version of the standard edition to be able to play with this new system.

    I'll also backport tests added to the old controller test in 2.3+.

    **Edit:** It now uses getter injection to benefit from lazy service loading by default.

    Commits
    -------

    1f2521e347 [FrameworkBundle] Introduce autowirable ControllerTrait