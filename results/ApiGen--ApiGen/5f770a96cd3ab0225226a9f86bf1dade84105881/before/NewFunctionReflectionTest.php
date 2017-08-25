<?php declare(strict_types=1);

namespace ApiGen\Reflection\Tests\Parser;

use ApiGen\Contracts\Parser\Reflection\FunctionReflectionInterface;
use ApiGen\Reflection\Parser\Parser;
use ApiGen\Tests\AbstractContainerAwareTestCase;
use phpDocumentor\Reflection\DocBlock\Tags\Return_;

final class NewFunctionReflectionTest extends AbstractContainerAwareTestCase
{
    /**
     * @var FunctionReflectionInterface
     */
    private $functionReflection;

    protected function setUp(): void
    {
        /** @var Parser $parser */
        $parser = $this->container->getByType(Parser::class);
        $parser->parseDirectories([__DIR__ . '/../../../../tests/Parser/Parser/ParserSource']);

        $functionReflections = $parser->getFunctionReflections();
        $this->functionReflection = array_pop($functionReflections);
    }

    public function testLines(): void
    {
        $this->assertSame(16, $this->functionReflection->getStartLine());
        $this->assertSame(18, $this->functionReflection->getEndLine());
    }

    public function testNames(): void
    {
        $this->assertSame('SomeNamespace\someAloneFunction', $this->functionReflection->getName());
        $this->assertSame('someAloneFunction', $this->functionReflection->getShortName());
        $this->assertSame('SomeNamespace\someAloneFunction()', $this->functionReflection->getPrettyName());
    }

    public function testNamespaces(): void
    {
        $this->assertSame('SomeNamespace', $this->functionReflection->getNamespaceName());
        $this->assertSame('SomeNamespace', $this->functionReflection->getPseudoNamespaceName());
        $this->testNames();
    }

    public function testAnnotations(): void
    {
        $this->assertCount(4, $this->functionReflection->getAnnotations());
        $this->assertTrue($this->functionReflection->hasAnnotation('return'));
        $this->assertTrue($this->functionReflection->hasAnnotation('param'));

        $returnAnnotation = $this->functionReflection->getAnnotation('return')[0];
        $this->assertInstanceOf(Return_::class, $returnAnnotation);
        $this->assertCount(3, $this->functionReflection->getAnnotation('param'));

        $this->assertFalse($this->functionReflection->isDeprecated());

        $this->assertSame(
            'Some description.' . PHP_EOL . PHP_EOL . 'And more lines!',
            $this->functionReflection->getDescription()
        );
    }

    public function testParameters(): void
    {
        $parameters = $this->functionReflection->getParameters();
        $this->assertCount(3, $parameters);
    }

    public function testMisc(): void
    {
        $this->assertTrue($this->functionReflection->isDocumented());
    }
}