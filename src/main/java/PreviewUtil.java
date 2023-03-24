import link.portalbox.pplib.type.MarketplacePlugin;
import link.portalbox.pplib.type.SpigetPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.List;
import java.util.*;

public class PreviewUtil {

    /* This awful class still needs converted. */

    public static void sendPreview(CommandSender player, SpigetPlugin spigetPlugin, boolean containDownloadPrompt) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m                                                       "));

        MarketplacePlugin marketplacePlugin = spigetPlugin.getMarketplacePlugin();

        String downloadUrl = spigetPlugin.externalUrl == null ? "https://api.spiget.org/v2/resources/" + spigetPlugin.id + "/download" : spigetPlugin.externalUrl;

        ArrayList<TextComponent> informationAsComponents = new ArrayList<>();
        try {
            TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Name: &b" + spigetPlugin.name));
            informationAsComponents.add(component);

            component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Description: &b&l[Hover Here]"));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.AQUA + spigetPlugin.description)));
            informationAsComponents.add(component);

            component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Downloads: &b" + String.format("%,d", spigetPlugin.downloads)));
            informationAsComponents.add(component);

            component = new TextComponent( ChatColor.translateAlternateColorCodes('&'," &7Rating: &b" + spigetPlugin.rating + "&e⭐"));
            informationAsComponents.add(component);

//            System.out.println(marketplacePlugin.supportedVersions);
//            System.out.println(new ArrayList<>(marketplacePlugin.supportedVersions));
//
//            component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Supported Versions: &b" + getVersionRange(new ArrayList<>(marketplacePlugin.supportedVersions))));
//            informationAsComponents.add(component);

            if (spigetPlugin.premium) {
                component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Premium: &b" + "Yes ($" + spigetPlugin.price + ")"));
                informationAsComponents.add(component);
            } else {
                component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7Premium: &b" + "No"));
                informationAsComponents.add(component);
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(spigetPlugin.updateDate * 1000L);

            component = new TextComponent( ChatColor.translateAlternateColorCodes('&'," &7Last Updated: &b" + toDateString(cal)));
            informationAsComponents.add(component);

            if (!downloadUrl.toString().contains("api.spiget.org")) {
                component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7External Link: &b"));

                TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&b&l[Click Here]"));
                link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, spigetPlugin.toString()));
                link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.AQUA + "Click to open the external download link")));

                component.addExtra(link);
                informationAsComponents.add(component);
            }

            component = new TextComponent(" ");
            informationAsComponents.add(component);

            component = new TextComponent(ChatColor.translateAlternateColorCodes('&', " &b&l[Click to Download]"));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pp install " + spigetPlugin.name));
            informationAsComponents.add(component);
        } catch (Exception exception) {
            exception.printStackTrace();
            informationAsComponents.add(new TextComponent(ChatColor.RED + " Error"));
        }



        try {
            String url = spigetPlugin.iconUrl.length() == 0 ? "https://cdn.discordapp.com/emojis/1065698008815112302.webp?size=128&quality=lossless" : spigetPlugin.iconUrl;

            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
            BufferedImage image = ImageIO.read(connection.getInputStream());

            // rows and columns
            int squareSize = 12;

            // array to hold sub-images
            BufferedImage[] imgs = new BufferedImage[squareSize * squareSize];

            // Equally dividing original image into images
            int subimage_Width = image.getWidth() / squareSize;
            int subimage_Height = image.getHeight() / squareSize;

            int current_img = 0;

            // iterating over rows and columns for each sub-image
            for (int i = 0; i < squareSize; i++) {
                for (int j = 0; j < squareSize; j++) {
                    // Creating sub image
                    imgs[current_img] = new BufferedImage(subimage_Width, subimage_Height, image.getType());
                    Graphics2D img_creator = imgs[current_img].createGraphics();

                    // coordinates of source image
                    int src_first_x = subimage_Width * j;
                    int src_first_y = subimage_Height * i;

                    // coordinates of sub-image
                    int dst_corner_x = subimage_Width * j + subimage_Width;
                    int dst_corner_y = subimage_Height * i + subimage_Height;

                    img_creator.drawImage(image, 0, 0, subimage_Width, subimage_Height, src_first_x, src_first_y, dst_corner_x, dst_corner_y, null);
                    current_img++;
                }
            }


            boolean pre116 = Integer.parseInt(Bukkit.getVersion().split("\\.")[1]) < 16;

            int i = 0;
            int row = 0;
            TextComponent base = new TextComponent("");
            for (BufferedImage bound : imgs) {
                if (i == squareSize) {
                    i = 0;
                    if (informationAsComponents.size() > row && informationAsComponents.get(row) != null) {
                        base.addExtra(informationAsComponents.get(row));
                    }
//                    base.addExtra("\n");

                    player.spigot().sendMessage(base);
                    base = new TextComponent("");
                    row++;
                }
                i++;
                Color color = getAverageColor(bound);
                if (pre116) {

           //         builder.append(ChatUtil.fromRGB(color.getRed(), color.getGreen(), color.getBlue()));
                } else {
                    TextComponent colorComp = new TextComponent("▉");
                    colorComp.setColor(ChatColor.of(color));
                    base.addExtra(colorComp);
                }
            }
            connection.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m                                                       "));
    }

    public static String toDateString(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH) + getDaySuffix(cal.get(Calendar.DAY_OF_MONTH)) + " " + getMonth(cal.get(Calendar.MONTH) + 1) + " " + cal.get(Calendar.YEAR);
    }

    public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month-1];
    }
    public static String getDaySuffix(int day) {
        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st" };
        return suffixes[day];
    }
    public static String getVersionRange(List<String> versions) {
        List<Integer> majorVersions = new ArrayList<>();
        List<Integer> minorVersions = new ArrayList<>();

        for (String version : versions) {
            String[] parts = version.split("\\.");
            majorVersions.add(Integer.parseInt(parts[0]));
            minorVersions.add(Integer.parseInt(parts[1]));
        }

        int minMajor = Collections.min(majorVersions);
        int maxMajor = Collections.max(majorVersions);
        int minMinor = Collections.min(minorVersions);
        int maxMinor = Collections.max(minorVersions);

        String minVersion = String.format("%d.%d", minMajor, minMinor);
        String maxVersion = String.format("%d.%d", maxMajor, maxMinor);

        return String.format("%s-%s", minVersion, maxVersion);
    }

    private static String formatVersion(double versionValue) {
        return String.format("%.1f", versionValue);
    }
    public static Color getAverageColor(BufferedImage bi) {
        int step = 5;
        int sampled = 0;
        long sumr = 0L;
        long sumg = 0L;
        long sumb = 0L;
        for (int x = 0; x < bi.getWidth(); ++x) {
            for (int y = 0; y < bi.getHeight(); ++y) {
                if (x % step != 0 || y % step != 0) continue;
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += (long)pixel.getRed();
                sumg += (long)pixel.getGreen();
                sumb += (long)pixel.getBlue();
                ++sampled;
            }
        }
        return new Color(Math.round(sumr / (long)sampled), Math.round(sumg / (long)sampled), Math.round(sumb / (long)sampled));
    }

}
