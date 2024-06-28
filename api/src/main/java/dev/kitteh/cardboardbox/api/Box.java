package dev.kitteh.cardboardbox.api;

import org.bukkit.inventory.ItemStack;

public interface Box {
    default boolean check(ItemStack itemStack) throws Exception {
        return itemStack.equals(this.deserializeItem(this.serializeItem(itemStack)));
    }

    ItemStack deserializeItem(byte[] data);

    byte[] serializeItem(ItemStack item);
}
