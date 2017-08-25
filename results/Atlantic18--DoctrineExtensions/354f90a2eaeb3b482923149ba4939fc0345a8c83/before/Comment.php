<?php

namespace Loggable\Fixture\Entity;

/**
 * @Entity
 * @gedmo:Loggable(actions={"create", "update"})
 */
class Comment
{
    /**
     * @Column(name="id", type="integer")
     * @Id
     * @GeneratedValue(strategy="IDENTITY")
     */
    private $id;

    /**
     * @Column(name="title", type="string", length=8)
     */
    private $title;


    public function __toString()
    {
        return $this->title;
    }

    public function getId()
    {
        return $this->id;
    }

    public function setTitle($title)
    {
        $this->title = $title;
    }

    public function getTitle()
    {
        return $this->title;
    }
}