package apputil;

public class AppDefinition
{
    private String name;
    private String iconPath;
    private String url;
    private int orderNumber;

    public AppDefinition(String name, int orderNumber, String iconPath, String url)
    {
        this.name = name;
        this.iconPath = iconPath;
        this.url = url;
        this.orderNumber = orderNumber;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getIconPath()
    {
        return iconPath;
    }

    public int getOrderNumber()
    {
        return orderNumber;
    }
}
