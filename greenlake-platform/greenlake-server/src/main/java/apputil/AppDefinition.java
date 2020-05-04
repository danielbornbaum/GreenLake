package apputil;

/**
 * Data structure that defines an app inside the platform
 */
public class AppDefinition
{
    private String name;
    private String iconPath;
    private String url;
    private int orderNumber;

    /**
     * @param name display name of the app
     * @param orderNumber position in which the app is displayed in the menu
     * @param iconPath path to the apps icon
     * @param url url to the app
     */
    public AppDefinition(String name, int orderNumber, String iconPath, String url)
    {
        this.name = name;
        this.iconPath = iconPath;
        this.url = url;
        this.orderNumber = orderNumber;
    }

    /**
     * @return display name of the app
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return url of the app
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return icon url of the app
     */
    public String getIconPath()
    {
        return iconPath;
    }

    /**
     * @return order number describing the position of the app inside the menu
     */
    public int getOrderNumber()
    {
        return orderNumber;
    }
}
