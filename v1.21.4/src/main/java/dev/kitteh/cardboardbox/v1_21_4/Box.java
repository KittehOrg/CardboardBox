package dev.kitteh.cardboardbox.v1_21_4;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class Box implements dev.kitteh.cardboardbox.api.Box {
    public boolean check(ItemStack itemStack) {
        return itemStack.equals(deserializeItem(serializeItem(itemStack)));
    }

    private static final int DATA_VERSION = CraftMagicNumbers.INSTANCE.getDataVersion();

    @Override
    public byte[] serializeItem(ItemStack item) {
        CompoundTag compound = (net.minecraft.nbt.CompoundTag) CraftItemStack.asNMSCopy(item).save(MinecraftServer.getServer().registryAccess());
        compound.putInt("DataVersion", DATA_VERSION);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(
                    compound,
                    outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return outputStream.toByteArray();
    }

    @Override
    public ItemStack deserializeItem(byte[] data) {
        net.minecraft.nbt.CompoundTag compound;
        try {
            compound = net.minecraft.nbt.NbtIo.readCompressed(
                    new java.io.ByteArrayInputStream(data), net.minecraft.nbt.NbtAccounter.unlimitedHeap()
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        final int dataVersion = compound.getInt("DataVersion");
        Preconditions.checkArgument(dataVersion <= DATA_VERSION, "Newer version (" + dataVersion + " > " + DATA_VERSION + ")! Server downgrades are not supported!");
        compound = (net.minecraft.nbt.CompoundTag) MinecraftServer.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, compound), dataVersion, dataVersion).getValue();
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.parse(MinecraftServer.getServer().registryAccess(), compound).orElseThrow());
    }
}
