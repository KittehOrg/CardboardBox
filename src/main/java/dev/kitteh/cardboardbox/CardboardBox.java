package dev.kitteh.cardboardbox;

import dev.kitteh.cardboardbox.api.Box;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardboardBox {
    private static boolean ready = false;
    private static boolean modernPaper = false;
    private static Exception exception;

    private static Box chosenBox;

    static {
        init();
    }

    private static void init() {
        try {
            ItemStack.class.getDeclaredMethod("deserializeBytes", byte[].class);
            ItemStack.class.getDeclaredMethod("serializeAsBytes");
            modernPaper = true;
            ready = true;
            return;
        } catch (Exception ignored) {
        }
        try {
            int mcVersion = getMcVersion();
            if (mcVersion < 808) {
                JavaPlugin.getProvidingPlugin(CardboardBox.class).getLogger().warning("CardboardBox could not identify Minecraft version.");
                ready = false;
            }
            if (mcVersion >= 2106) {
                chosenBox = getBox("v1_21_6");
            } else if (mcVersion == 2105) {
                chosenBox = getBox("v1_21_5");
            } else if (mcVersion == 2104) {
                chosenBox = getBox("v1_21_4");
            } else if (mcVersion == 2103) {
                chosenBox = getBox("v1_21_3");
            } else if (mcVersion >= 2100) {
                chosenBox = getBox("v1_21");
            } else if (mcVersion == 2006) {
                chosenBox = getBox("v1_20_6");
            } else {
                chosenBox = new dev.kitteh.cardboardbox.pre_v1_20_6.Box();
            }
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = itemStack.getItemMeta();
            meta.addEnchant(Enchantment.KNOCKBACK, 3, false);
            meta.setDisplayName("Meow meow!");
            meta.setLore(Arrays.asList("New", "line", "  here"));
            itemStack.setItemMeta(meta);
            if (!itemStack.equals(chosenBox.deserializeItem(chosenBox.serializeItem(itemStack)))) {
                throw new IllegalStateException("Failed to deserialize serialized content properly");
            }
            ready = true;
        } catch (Exception e) {
            exception = e;
        }
    }

    private static Box getBox(String ver) throws Exception {
        return (Box) Class.forName(CardboardBox.class.getPackage().getName() + "." + ver + ".Box").getConstructor().newInstance();
    }

    private static int getMcVersion() {
        Pattern versionPattern = Pattern.compile("1\\.(\\d{1,2})(?:\\.(\\d{1,2}))?");
        Matcher versionMatcher = versionPattern.matcher(Bukkit.getVersion());

        int mcVersion = 0;
        if (versionMatcher.find()) {
            try {
                int minor = Integer.parseInt(versionMatcher.group(1));
                String patchS = versionMatcher.group(2);
                int patch = (patchS == null || patchS.isEmpty()) ? 0 : Integer.parseInt(patchS);
                mcVersion = (minor * 100) + patch;
            } catch (NumberFormatException ignored) {
            }
        }
        return mcVersion;
    }

    /**
     * Gets if Cardboard Box will just use Paper's built-in functionality.
     *
     * @return true if the built-in methods exist, eliminating stress
     */
    public static boolean isModernPaperSupport() {
        return modernPaper;
    }

    /**
     * Gets if Cardboard Box is ready to work, or failed to initialize.
     *
     * @return true if ready
     */
    public static boolean isReady() {
        return ready;
    }

    /**
     * Gets the exception showing the failure to load, if not ready.
     *
     * @return exception if failed to load properly
     */
    public static Exception getException() {
        return exception;
    }

    /**
     * Serializes an ItemStack to bytes. Will store air and null the same, as
     * just a single byte of 0x0.
     *
     * @param item item to serialize
     * @return bytes of the item serialized
     * @throws IllegalStateException if CardboardBox failed to initialize
     *                               (check with {@link #isReady()})
     * @throws RuntimeException      if serialization failed for any reason
     */
    public static byte[] serializeItem(ItemStack item) {
        if (!ready) {
            throw new IllegalStateException("Cardboard Box failed to initialize. Cannot serialize without risk.", exception);
        }
        if (item == null || item.getType() == Material.AIR) {
            return new byte[]{0x0};
        }
        if (modernPaper) {
            return item.serializeAsBytes();
        }
        return chosenBox.serializeItem(item);
    }

    /**
     * Deserializes an ItemStack previously serialized by Cardboard Box. Will
     * return air for a null or empty array of data.
     *
     * @param data data to deserialize
     * @return the ItemStack
     * @throws IllegalArgumentException if the stored item's version is
     *                                  greater than the current server data version
     * @throws IllegalStateException    if CardboardBox failed to initialize
     *                                  (check with {@link #isReady()})
     * @throws RuntimeException         if deserialization failed for any reason
     */
    public static ItemStack deserializeItem(byte[] data) {
        if (!ready) {
            throw new IllegalStateException("Cardboard Box failed to initialize. Cannot serialize without risk.", exception);
        }
        if (data == null || data.length == 0 || (data.length == 1 && data[0] == 0x0)) {
            return new ItemStack(Material.AIR);
        }
        if (modernPaper) {
            return ItemStack.deserializeBytes(data);
        }
        return chosenBox.deserializeItem(data);
    }
}