package dev.kitteh.cardboardbox.v26_1;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class Box implements dev.kitteh.cardboardbox.api.Box {
    public boolean check(ItemStack itemStack) {
        return itemStack.equals(deserializeItem(serializeItem(itemStack)));
    }

    private static final int DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();

    @Override
    public byte[] serializeItem(ItemStack item) {
        CompoundTag compound = (CompoundTag) net.minecraft.world.item.ItemStack.CODEC.encodeStart(
                MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                CraftItemStack.asNMSCopy(item)
        ).getOrThrow();
        compound.putInt("DataVersion", DATA_VERSION);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(
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
        CompoundTag compound;
        try {
            compound = NbtIo.readCompressed(
                    new java.io.ByteArrayInputStream(data), NbtAccounter.unlimitedHeap()
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        final int dataVersion = compound.getIntOr("DataVersion", 0);
        Preconditions.checkArgument(dataVersion <= DATA_VERSION, "Newer version (" + dataVersion + " > " + DATA_VERSION + ")! Server downgrades are not supported!");
        compound = (CompoundTag) MinecraftServer.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, compound), dataVersion, dataVersion).getValue();
        if (compound.getStringOr("id", "minecraft:air").equals("minecraft:air")) {
            return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.EMPTY);
        }
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.CODEC.parse(
                CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), compound
        ).getOrThrow());
    }
}
