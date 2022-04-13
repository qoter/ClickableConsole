using JetBrains.Application.BuildScript.Application.Zones;

namespace ReSharperPlugin.ClickableConsole
{
    [ZoneMarker]
    public class ZoneMarker : IRequire<IClickableConsoleZone>
    {
    }
}