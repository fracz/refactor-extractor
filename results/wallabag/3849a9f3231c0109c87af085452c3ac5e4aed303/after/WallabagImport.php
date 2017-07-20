<?php

namespace Wallabag\ImportBundle\Import;

use Wallabag\CoreBundle\Entity\Entry;

abstract class WallabagImport extends AbstractImport
{
    protected $skippedEntries = 0;
    protected $importedEntries = 0;
    protected $filepath;
    // untitled in all languages from v1
    protected $untitled = [
        'Untitled',
        'Sans titre',
        'podle nadpisu',
        'Sin título',
        'با عنوان',
        'per titolo',
        'Sem título',
        'Без названия',
        'po naslovu',
        'Без назви',
        'No title found',
        '',
    ];

    /**
     * {@inheritdoc}
     */
    abstract public function getName();

    /**
     * {@inheritdoc}
     */
    abstract public function getUrl();

    /**
     * {@inheritdoc}
     */
    abstract public function getDescription();

    /**
     * {@inheritdoc}
     */
    public function import()
    {
        if (!$this->user) {
            $this->logger->error('WallabagImport: user is not defined');

            return false;
        }

        if (!file_exists($this->filepath) || !is_readable($this->filepath)) {
            $this->logger->error('WallabagImport: unable to read file', ['filepath' => $this->filepath]);

            return false;
        }

        $data = json_decode(file_get_contents($this->filepath), true);

        if (empty($data)) {
            return false;
        }

        if ($this->producer) {
            $this->parseEntriesForProducer($data);

            return true;
        }

        $this->parseEntries($data);

        return true;
    }

    /**
     * {@inheritdoc}
     */
    public function getSummary()
    {
        return [
            'skipped' => $this->skippedEntries,
            'imported' => $this->importedEntries,
        ];
    }

    /**
     * Set file path to the json file.
     *
     * @param string $filepath
     */
    public function setFilepath($filepath)
    {
        $this->filepath = $filepath;

        return $this;
    }

    /**
     * {@inheritdoc}
     */
    public function parseEntry(array $importedEntry)
    {
        $existingEntry = $this->em
            ->getRepository('WallabagCoreBundle:Entry')
            ->findByUrlAndUserId($importedEntry['url'], $this->user->getId());

        if (false !== $existingEntry) {
            ++$this->skippedEntries;

            return;
        }

        $data = $this->prepareEntry($importedEntry);

        $entry = $this->fetchContent(
            new Entry($this->user),
            $importedEntry['url'],
            $data
        );

        // jump to next entry in case of problem while getting content
        if (false === $entry) {
            ++$this->skippedEntries;

            return;
        }

        if (array_key_exists('tags', $data)) {
            $this->contentProxy->assignTagsToEntry(
                $entry,
                $data['tags']
            );
        }

        if (isset($importedEntry['preview_picture'])) {
            $entry->setPreviewPicture($importedEntry['preview_picture']);
        }

        $entry->setArchived($data['is_archived']);
        $entry->setStarred($data['is_starred']);

        $this->em->persist($entry);
        ++$this->importedEntries;

        return $entry;
    }

    /**
     * This should return a cleaned array for a given entry to be given to `updateEntry`.
     *
     * @param array $entry Data from the imported file
     *
     * @return array
     */
    abstract protected function prepareEntry($entry = []);
}