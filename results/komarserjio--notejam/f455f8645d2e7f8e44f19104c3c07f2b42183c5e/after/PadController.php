<?php

namespace Notejam\NoteBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Notejam\NoteBundle\Entity\Pad;
use Notejam\NoteBundle\Form\Type\PadType;

class PadController extends Controller
{
    public function listAction()
    {
        $user = $this->get('security.context')->getToken()->getUser();
        return $this->render(
            'NotejamNoteBundle:Pad:list.html.twig',
            array('pads' => $user->getPads())
        );
    }

    public function notesAction($id, Request $request)
    {
        $user = $this->get('security.context')->getToken()->getUser();
        $orderBy = $request->query->get('order', 'name');

        $pad = $this->getDoctrine()
                    ->getRepository('NotejamNoteBundle:Pad')
                    ->findOneBy(array('id' => $id,
                                      'user' => $user));
        if (!$pad) {
            throw $this->createNotFoundException('The pad does not exist');
        }

        $notes = $this->getDoctrine()
                      ->getRepository('NotejamNoteBundle:Note')
                      ->findBy(array('pad' => $id),
                               $this->_buildOrderBy($orderBy));

        return $this->render('NotejamNoteBundle:Pad:notes.html.twig', array(
            'notes' => $notes, 'pad' => $pad
        ));
    }

    private function _buildOrderBy($orderBy = 'name')
    {
        return array(
            'name' => array('name' => 'ASC'),
            '-name' => array('name' => 'DESC')
        )[$orderBy];
    }

    public function createAction(Request $request)
    {
        $form = $this->createForm(new PadType(), new Pad());
        if ($request->getMethod() == 'POST') {
            $form->handleRequest($request);
            if ($form->isValid()) {
                $pad = $form->getData();
                $pad->setUser(
                    $this->get('security.context')->getToken()->getUser()
                );

                $em = $this->getDoctrine()->getManager();
                $em->persist($pad);
                $em->flush();

                $this->get('session')->getFlashBag()->add(
                    'success',
                    'Pad is successfully created'
                );
                return $this->redirect($this->generateUrl('homepage'));
            }
        }
        return $this->render(
            'NotejamNoteBundle:Pad:create.html.twig',
            array('form' => $form->createView())
        );
    }

    public function editAction($id, Request $request)
    {
        $user = $this->get('security.context')->getToken()->getUser();
        $pad = $this->getDoctrine()
                    ->getRepository('NotejamNoteBundle:Pad')
                    ->findOneBy(array('id' => $id,
                                      'user' => $user));
        if (!$pad) {
            throw $this->createNotFoundException('The pad does not exist');
        }

        $form = $this->createForm(new PadType(), $pad);
        if ($request->getMethod() == 'POST') {
            $form->handleRequest($request);
            if ($form->isValid()) {
                $em = $this->getDoctrine()->getManager();
                $em->persist($form->getData());
                $em->flush();

                $this->get('session')->getFlashBag()->add(
                    'success',
                    'Pad is successfully updated'
                );
                return $this->redirect($this->generateUrl('homepage'));
            }
        }

        return $this->render(
            'NotejamNoteBundle:Pad:edit.html.twig',
            array('form' => $form->createView(), 'pad' => $pad)
        );
    }

    public function deleteAction($id, Request $request)
    {
        $user = $this->get('security.context')->getToken()->getUser();
        $pad = $this->getDoctrine()
                    ->getRepository('NotejamNoteBundle:Pad')
                    ->findOneBy(array('id' => $id,
                                      'user' => $user));
        if (!$pad) {
            throw $this->createNotFoundException('The pad does not exist');
        }
        if ($request->getMethod() == 'POST') {
            $em = $this->getDoctrine()->getManager();
            $em->remove($pad);
            $em->flush();

            $this->get('session')->getFlashBag()->add(
                'success',
                'Pad is successfully deleted'
            );
            return $this->redirect($this->generateUrl('homepage'));
        }
        return $this->render(
            'NotejamNoteBundle:Pad:delete.html.twig',
            array('pad' => $pad)
        );
    }
}
