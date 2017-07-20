<?php

namespace Model;

require_once __DIR__.'/base.php';

use SimpleValidator\Validator;
use SimpleValidator\Validators;

/**
 * Category model
 *
 * @package  model
 * @author   Frederic Guillot
 */
class Category extends Base
{
    /**
     * SQL table name
     *
     * @var string
     */
    const TABLE = 'project_has_categories';

    /**
     * Get a category by the id
     *
     * @access public
     * @param  integer   $category_id    Category id
     * @return array
     */
    public function getById($category_id)
    {
        return $this->db->table(self::TABLE)->eq('id', $category_id)->findOne();
    }

    /**
     * Return the list of all categories
     *
     * @access public
     * @param  integer   $project_id    Project id
     * @param  bool      $prepend_none  If true, prepend to the list the value 'None'
     * @param  bool      $prepend_all   If true, prepend to the list the value 'All'
     * @return array
     */
    public function getList($project_id, $prepend_none = true, $prepend_all = false)
    {
        $listing = $this->db->table(self::TABLE)
            ->eq('project_id', $project_id)
            ->asc('name')
            ->listing('id', 'name');

        $prepend = array();

        if ($prepend_all) {
            $prepend[-1] = t('All categories');
        }

        if ($prepend_none) {
            $prepend[0] = t('No category');
        }

        return $prepend + $listing;
    }

    /**
     * Create a category
     *
     * @access public
     * @param  array    $values    Form values
     * @return bool
     */
    public function create(array $values)
    {
        return $this->db->table(self::TABLE)->save($values);
    }

    /**
     * Update a category
     *
     * @access public
     * @param  array    $values    Form values
     * @return bool
     */
    public function update(array $values)
    {
        return $this->db->table(self::TABLE)->eq('id', $values['id'])->save($values);
    }

    /**
     * Remove a category
     *
     * @access public
     * @param  integer   $category_id    Category id
     * @return bool
     */
    public function remove($category_id)
    {
        $this->db->startTransaction();
        $r1 = $this->db->table(Task::TABLE)->eq('category_id', $category_id)->update(array('category_id' => 0));
        $r2 = $this->db->table(self::TABLE)->eq('id', $category_id)->remove();
        $this->db->closeTransaction();

        return $r1 && $r2;
    }

    /**
     * Validate category creation
     *
     * @access public
     * @param  array   $array            Form values
     * @return array   $valid, $errors   [0] = Success or not, [1] = List of errors
     */
    public function validateCreation(array $values)
    {
        $v = new Validator($values, array(
            new Validators\Required('project_id', t('The project id is required')),
            new Validators\Integer('project_id', t('The project id must be an integer')),
            new Validators\Required('name', t('The name is required')),
            new Validators\MaxLength('name', t('The maximum length is %d characters', 50), 50)
        ));

        return array(
            $v->execute(),
            $v->getErrors()
        );
    }

    /**
     * Validate category modification
     *
     * @access public
     * @param  array   $array            Form values
     * @return array   $valid, $errors   [0] = Success or not, [1] = List of errors
     */
    public function validateModification(array $values)
    {
        $v = new Validator($values, array(
            new Validators\Required('id', t('The id is required')),
            new Validators\Integer('id', t('The id must be an integer')),
            new Validators\Required('project_id', t('The project id is required')),
            new Validators\Integer('project_id', t('The project id must be an integer')),
            new Validators\Required('name', t('The name is required')),
            new Validators\MaxLength('name', t('The maximum length is %d characters', 50), 50)
        ));

        return array(
            $v->execute(),
            $v->getErrors()
        );
    }
}