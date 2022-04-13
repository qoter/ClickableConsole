using System.Threading;
using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ReSharper.Feature.Services;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.ReSharper.TestFramework;
using JetBrains.TestFramework;
using JetBrains.TestFramework.Application.Zones;
using NUnit.Framework;

[assembly: Apartment(ApartmentState.STA)]

namespace ReSharperPlugin.ClickableConsole.Tests
{

    [ZoneDefinition]
    public class ClickableConsoleTestEnvironmentZone : ITestsEnvZone, IRequire<PsiFeatureTestZone>, IRequire<IClickableConsoleZone> { }

    [ZoneMarker]
    public class ZoneMarker : IRequire<ICodeEditingZone>, IRequire<ILanguageCSharpZone>, IRequire<ClickableConsoleTestEnvironmentZone> { }
    
    [SetUpFixture]
    public class ClickableConsoleTestsAssembly : ExtensionTestEnvironmentAssembly<ClickableConsoleTestEnvironmentZone> { }
}