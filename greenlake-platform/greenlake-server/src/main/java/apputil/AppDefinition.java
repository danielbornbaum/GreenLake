package apputil;

public class AppDefinition
{
    String translationKey;
    String iconPath;
    String url;

    public AppDefinition(String translationKey, String iconPath, String url)
    {
        this.translationKey = translationKey;
        this.iconPath = iconPath;
        this.url = url;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }

    public String getUrl()
    {
        return url;
    }

    public String getIconPath()
    {
        return iconPath;
    }
}
