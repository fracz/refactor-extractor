commit 4eaa4bffd21d00908aca498d9da1b655c4734e76
Merge: 921b6aa 351174b
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Jun 19 17:19:52 2015 +0200

    Merge branch '2.8'

    * 2.8:
      [2.8] Silence newest deprecations
      [FrameworkBundle] Reuse PropertyAccessor service for ObjectNormalizer
      [VarDumper] Fix dump output for better readability
      [PhpUnitBridge] Enforce @-silencing of deprecation notices according to new policy

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Tests/Console/Descriptor/AbstractDescriptorTest.php
            src/Symfony/Bundle/FrameworkBundle/Tests/DependencyInjection/Compiler/LegacyTemplatingAssetHelperPassTest.php
            src/Symfony/Bundle/FrameworkBundle/Tests/DependencyInjection/FrameworkExtensionTest.php
            src/Symfony/Bundle/FrameworkBundle/Tests/Templating/Helper/AssetsHelperTest.php
            src/Symfony/Bundle/TwigBundle/Tests/DependencyInjection/TwigExtensionTest.php
            src/Symfony/Bundle/TwigBundle/Tests/Extension/LegacyAssetsExtensionTest.php
            src/Symfony/Bundle/TwigBundle/Tests/TokenParser/LegacyRenderTokenParserTest.php
            src/Symfony/Component/ClassLoader/Tests/LegacyUniversalClassLoaderTest.php
            src/Symfony/Component/Console/Tests/ApplicationTest.php
            src/Symfony/Component/Console/Tests/Command/CommandTest.php
            src/Symfony/Component/Console/Tests/Helper/LegacyDialogHelperTest.php
            src/Symfony/Component/Console/Tests/Helper/LegacyProgressHelperTest.php
            src/Symfony/Component/Console/Tests/Helper/LegacyTableHelperTest.php
            src/Symfony/Component/Console/Tests/Input/InputDefinitionTest.php
            src/Symfony/Component/Console/Tests/Input/StringInputTest.php
            src/Symfony/Component/Debug/Tests/ErrorHandlerTest.php
            src/Symfony/Component/Debug/Tests/FatalErrorHandler/ClassNotFoundFatalErrorHandlerTest.php
            src/Symfony/Component/DependencyInjection/Tests/Compiler/CheckDefinitionValidityPassTest.php
            src/Symfony/Component/DependencyInjection/Tests/Compiler/LegacyResolveParameterPlaceHoldersPassTest.php
            src/Symfony/Component/DependencyInjection/Tests/ContainerBuilderTest.php
            src/Symfony/Component/DependencyInjection/Tests/DefinitionDecoratorTest.php
            src/Symfony/Component/DependencyInjection/Tests/DefinitionTest.php
            src/Symfony/Component/DependencyInjection/Tests/Dumper/GraphvizDumperTest.php
            src/Symfony/Component/DependencyInjection/Tests/Dumper/PhpDumperTest.php
            src/Symfony/Component/DependencyInjection/Tests/Dumper/XmlDumperTest.php
            src/Symfony/Component/DependencyInjection/Tests/Dumper/YamlDumperTest.php
            src/Symfony/Component/DependencyInjection/Tests/LegacyContainerBuilderTest.php
            src/Symfony/Component/DependencyInjection/Tests/LegacyDefinitionTest.php
            src/Symfony/Component/DependencyInjection/Tests/Loader/XmlFileLoaderTest.php
            src/Symfony/Component/DependencyInjection/Tests/Loader/YamlFileLoaderTest.php
            src/Symfony/Component/EventDispatcher/Tests/AbstractEventDispatcherTest.php
            src/Symfony/Component/EventDispatcher/Tests/EventTest.php
            src/Symfony/Component/Form/Tests/Extension/HttpFoundation/EventListener/LegacyBindRequestListenerTest.php
            src/Symfony/Component/HttpFoundation/Tests/Session/Flash/FlashBagTest.php
            src/Symfony/Component/HttpFoundation/Tests/Session/Storage/Handler/LegacyPdoSessionHandlerTest.php
            src/Symfony/Component/HttpKernel/Tests/DependencyInjection/FragmentRendererPassTest.php
            src/Symfony/Component/HttpKernel/Tests/EventListener/ProfilerListenerTest.php
            src/Symfony/Component/HttpKernel/Tests/KernelTest.php
            src/Symfony/Component/Locale/Tests/LocaleTest.php
            src/Symfony/Component/Locale/Tests/Stub/StubLocaleTest.php
            src/Symfony/Component/OptionsResolver/Tests/LegacyOptionsResolverTest.php
            src/Symfony/Component/OptionsResolver/Tests/LegacyOptionsTest.php
            src/Symfony/Component/Process/Tests/AbstractProcessTest.php
            src/Symfony/Component/Routing/Tests/Annotation/RouteTest.php
            src/Symfony/Component/Routing/Tests/Generator/UrlGeneratorTest.php
            src/Symfony/Component/Routing/Tests/Loader/XmlFileLoaderTest.php
            src/Symfony/Component/Routing/Tests/Loader/YamlFileLoaderTest.php
            src/Symfony/Component/Routing/Tests/Matcher/Dumper/LegacyApacheMatcherDumperTest.php
            src/Symfony/Component/Routing/Tests/Matcher/LegacyApacheUrlMatcherTest.php
            src/Symfony/Component/Routing/Tests/RouteTest.php
            src/Symfony/Component/Serializer/Tests/Normalizer/GetSetMethodNormalizerTest.php
            src/Symfony/Component/Serializer/Tests/Normalizer/ObjectNormalizerTest.php
            src/Symfony/Component/Serializer/Tests/Normalizer/PropertyNormalizerTest.php
            src/Symfony/Component/Templating/Tests/Helper/LegacyAssetsHelperTest.php
            src/Symfony/Component/Templating/Tests/Helper/LegacyCoreAssetsHelperTest.php
            src/Symfony/Component/Templating/Tests/Loader/LoaderTest.php
            src/Symfony/Component/Yaml/Tests/YamlTest.php