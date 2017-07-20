<?php

namespace Wallabag\ImportBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Symfony\Component\HttpFoundation\Request;
use Wallabag\ImportBundle\Form\Type\UploadImportType;

class WallabagV1Controller extends Controller
{
    /**
     * @Route("/import/wallabag-v1", name="import_wallabag_v1")
     */
    public function indexAction(Request $request)
    {
        $importForm = $this->createForm(new UploadImportType());
        $importForm->handleRequest($request);
        $user = $this->getUser();

        if ($importForm->isValid()) {
            $file = $importForm->get('file')->getData();
            $name = $user->getId().'.json';

            if (in_array($file->getClientMimeType(), $this->getParameter('wallabag_import.allow_mimetypes')) && $file->move($this->getParameter('wallabag_import.resource_dir'), $name)) {
                $wallabag = $this->get('wallabag_import.wallabag_v1.import');
                $res = $wallabag
                    ->setUser($this->getUser())
                    ->setFilepath($this->getParameter('wallabag_import.resource_dir').'/'.$name)
                    ->import();

                $message = 'Import failed, please try again.';
                if (true === $res) {
                    $summary = $wallabag->getSummary();
                    $message = 'Import summary: '.$summary['imported'].' imported, '.$summary['skipped'].' already saved.';

                    @unlink($this->getParameter('wallabag_import.resource_dir').'/'.$name);
                }

                $this->get('session')->getFlashBag()->add(
                    'notice',
                    $message
                );

                return $this->redirect($this->generateUrl('homepage'));
            } else {
                $this->get('session')->getFlashBag()->add(
                    'notice',
                    'Error while processing import. Please verify your import file.'
                );
            }
        }

        return $this->render('WallabagImportBundle:WallabagV1:index.html.twig', [
            'form' => $importForm->createView(),
        ]);
    }
}