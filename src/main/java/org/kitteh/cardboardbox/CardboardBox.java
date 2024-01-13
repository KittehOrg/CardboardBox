/*
 * Cardboard Box
 * Copyright (C) 2020-2024 Matt Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kitteh.cardboardbox;

import com.mojang.datafixers.DSL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cardboard Box!
 */
@SuppressWarnings("UnqualifiedStaticUsage")
public class CardboardBox {
    private static final int OLD_DATA_VERSION = 1343;

    private static Method itemStackFromCompound;
    private static Method itemStackSave;
    private static Method nbtCompressedStreamToolsRead;
    private static Method nbtCompressedStreamToolsWrite;
    private static Method nbtTagCompoundGetInt;
    private static Method nbtTagCompoundSetInt;
    private static Method dataFixerUpdate;
    private static Method nbtAccounterUnlimitedHeap;
    private static Constructor<?> dynamic;
    private static Constructor<?> nbtTagCompoundConstructor;
    private static Constructor<?> itemStackConstructor;

    private static Class<?> craftItemStack;
    private static Field craftItemStackHandle;
    private static Method craftItemStackAsNMSCopy;
    private static Method craftItemStackAsCraftMirror;
    private static Method dynamicGetValue;

    private static Object dynamicOpsNBT;
    private static Object dataConverterTypesItemStack;
    private static Object dataConverterRegistryDataFixer;

    private static int dataVersion;
    private static boolean hasDataVersion = false;

    private static boolean failure = true;
    private static boolean modernPaper = false;
    private static Exception exception;
    private static boolean testPending = true;

    static {
        init();
    }

    private static void init() {
        try {
            ItemStack.class.getDeclaredMethod("deserializeBytes", byte[].class);
            ItemStack.class.getDeclaredMethod("serializeAsBytes");
            modernPaper = true;
            failure = false;
            return;
        } catch (Exception ignored) {
        }
        try {
            Pattern versionPattern = Pattern.compile("1\\.(\\d{1,2})(?:\\.(\\d{1,2}))?");
            Matcher versionMatcher = versionPattern.matcher(Bukkit.getVersion());
            if (!versionMatcher.find()) {
                throw new RuntimeException("Could not parse version");
            }
            int minor = Integer.parseInt(versionMatcher.group(1));
            String patchS = versionMatcher.group(2);
            int patch = (patchS == null || patchS.isEmpty()) ? 0 : Integer.parseInt(patchS);
            int ver = (minor * 100) + patch;
            String[] packageSplit = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            String packageVersion = packageSplit[packageSplit.length - 1] + '.';

            String obc = "org.bukkit.craftbukkit." + packageVersion;
            String nmsItemStack, nmsNBTCompressedStreamTools, nmsNBTTagCompound, nmsDynamicOpsNBT, nmsDataConverterTypes, nmsDataConverterRegistry;
            String nmsNBTReadLimiter = "net.minecraft.nbt.NBTReadLimiter";

            if (ver < 1700) {
                String nms = "net.minecraft.server." + packageVersion;
                nmsItemStack = nms + "ItemStack";
                nmsNBTTagCompound = nms + "NBTTagCompound";
                nmsNBTCompressedStreamTools = nms + "NBTCompressedStreamTools";
                nmsDynamicOpsNBT = nms + "DynamicOpsNBT";
                nmsDataConverterTypes = nms + "DataConverterTypes";
                nmsDataConverterRegistry = nms + "DataConverterRegistry";
            } else {
                nmsItemStack = "net.minecraft.world.item.ItemStack";
                nmsNBTTagCompound = "net.minecraft.nbt.NBTTagCompound";
                nmsNBTCompressedStreamTools = "net.minecraft.nbt.NBTCompressedStreamTools";
                nmsDynamicOpsNBT = "net.minecraft.nbt.DynamicOpsNBT";
                nmsDataConverterTypes = "net.minecraft.util.datafix.fixes.DataConverterTypes";
                nmsDataConverterRegistry = "net.minecraft.util.datafix.DataConverterRegistry";
            }


            // Time to load ALL the things!

            // NMS
            Class<?> itemStack = Class.forName(nmsItemStack);
            Class<?> nbtCompressedStreamTools = Class.forName(nmsNBTCompressedStreamTools);
            Class<?> nbtTagCompound = Class.forName(nmsNBTTagCompound);
            nbtTagCompoundConstructor = nbtTagCompound.getConstructor();
            try {
                itemStackFromCompound = itemStack.getMethod("fromCompound", nbtTagCompound);
            } catch (NoSuchMethodException e) {
                try {
                    itemStackFromCompound = itemStack.getMethod("createStack", nbtTagCompound);
                } catch (NoSuchMethodException e2) {
                    try {
                        itemStackFromCompound = itemStack.getMethod("a", nbtTagCompound);
                    } catch (NoSuchMethodException e3) {
                        itemStackConstructor = itemStack.getConstructor(nbtTagCompound);
                    }
                }
            }
            itemStackSave = itemStack.getMethod(ver >= 1800 ? "b" : "save", nbtTagCompound);
            if (ver < 2004) {
                nbtCompressedStreamToolsRead = nbtCompressedStreamTools.getMethod("a", InputStream.class);
            } else {
                Class<?> nbtAccounter = Class.forName(nmsNBTReadLimiter);
                nbtCompressedStreamToolsRead = nbtCompressedStreamTools.getMethod("a", InputStream.class, nbtAccounter);
                nbtAccounterUnlimitedHeap = nbtAccounter.getMethod("a");
            }
            nbtCompressedStreamToolsWrite = nbtCompressedStreamTools.getMethod("a", nbtTagCompound, OutputStream.class);
            nbtTagCompoundGetInt = nbtTagCompound.getMethod(ver >= 1800 ? "h" : "getInt", String.class);
            nbtTagCompoundSetInt = nbtTagCompound.getMethod(ver >= 1800 ? "a" : "setInt", String.class, int.class);

            // OBC
            craftItemStack = Class.forName(obc + "inventory.CraftItemStack");
            Class<?> craftMagicNumbers = Class.forName(obc + "util.CraftMagicNumbers");
            craftItemStackHandle = craftItemStack.getDeclaredField("handle");
            craftItemStackHandle.setAccessible(true);
            Field craftMagicNumbersInstance = craftMagicNumbers.getField("INSTANCE");
            craftItemStackAsNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            craftItemStackAsCraftMirror = craftItemStack.getMethod("asCraftMirror", itemStack);

            // DataFixer
            try {
                craftMagicNumbers.getMethod("getDataVersion");
                dataVersion = (int) craftMagicNumbers.getMethod("getDataVersion").invoke(craftMagicNumbersInstance.get(null));
                Class<?> dataFixer = Class.forName("com.mojang.datafixers.DataFixer");
                for (Method method : dataFixer.getMethods()) {
                    if (method.getName().equals("update") && method.getParameterCount() == 4) {
                        dataFixerUpdate = method;
                        break;
                    }
                }

                Class<?> dataConverterRegistry = Class.forName(nmsDataConverterRegistry);
                for (Field field : dataConverterRegistry.getDeclaredFields()) {
                    if (dataFixer.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        dataConverterRegistryDataFixer = field.get(null);
                        break;
                    }
                }
                if (dataConverterRegistryDataFixer == null) {
                    throw new IllegalStateException("No sign of data fixer");
                }
                if (ver < 1700) {
                    dataConverterTypesItemStack = Class.forName(nmsDataConverterTypes).getField("ITEM_STACK").get(null);
                } else {
                    dataConverterTypesItemStack = (DSL.TypeReference) () -> "item_stack";
                }
                dynamicOpsNBT = Class.forName(nmsDynamicOpsNBT).getField("a").get(null);
                Class<?> dynamicClass = dataFixerUpdate.getParameterTypes()[1];
                for (Constructor<?> constructor : dynamicClass.getConstructors()) {
                    if (constructor.getParameterCount() == 2) {
                        dynamic = constructor;
                        break;
                    }
                }
                dynamicGetValue = dynamicClass.getMethod("getValue");
                hasDataVersion = true;
            } catch (Exception ignored) {
            }
            failure = false;
        } catch (Exception e) {
            exception = e;
        }
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
        if (modernPaper) {
            return true;
        }
        if (!failure && testPending) {
            testPending = false;
            try {
                // TODO come up with an insane itemstack to deserialize and reserialize as a test.
                ItemStack itemStack = new ItemStack(Material.DIRT);
                deserializeItem(serializeItem(itemStack));
            } catch (Exception e) {
                failure = true;
                exception = e;
            }
        }
        return !failure;
    }

    // The below are modeled on the work of Mariell Hoversholm (Proximyst)
    //  released under GPLv3 for the Paper project

    /**
     * Serializes an ItemStack to bytes. Will store air and null the same, as
     * just a single byte of 0x0.
     *
     * @param item item to serialize
     * @return bytes of the item serialized
     * @throws IllegalStateException if CardboardBox failed to initialize
     * (check with {@link #isReady()})
     * @throws RuntimeException if serialization failed for any reason
     */
    public static byte[] serializeItem(ItemStack item) {
        if (failure) {
            throw new IllegalStateException("Cardboard Box failed to initialize. Cannot serialize without risk.", exception);
        }
        if (item == null || item.getType() == Material.AIR) {
            return new byte[]{0x0};
        }
        if (modernPaper) {
            return item.serializeAsBytes();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Object nmsItem;
            if (craftItemStack.isAssignableFrom(item.getClass())) {
                nmsItem = craftItemStackHandle.get(item);
            } else {
                nmsItem = craftItemStackAsNMSCopy.invoke(null, item);
            }
            Object compound = itemStackSave.invoke(nmsItem, nbtTagCompoundConstructor.newInstance());
            if (hasDataVersion) {
                nbtTagCompoundSetInt.invoke(compound, "DataVersion", dataVersion);
            }
            nbtCompressedStreamToolsWrite.invoke(null, compound, outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize item stack\n" + item, e);
        }
        return outputStream.toByteArray();
    }

    /**
     * Deserializes an ItemStack previously serialized by Cardboard Box. Will
     * return air for a null or empty array of data.
     *
     * @param data data to deserialize
     * @return the ItemStack
     * @throws IllegalArgumentException if the stored item's version is
     * greater than the current server data version
     * @throws IllegalStateException if CardboardBox failed to initialize
     * (check with {@link #isReady()})
     * @throws RuntimeException if deserialization failed for any reason
     */
    public static ItemStack deserializeItem(byte[] data) {
        if (failure) {
            throw new IllegalStateException("Cardboard Box failed to initialize. Cannot serialize without risk.", exception);
        }
        if (data == null || data.length == 0 || (data.length == 1 && data[0] == 0x0)) {
            return new ItemStack(Material.AIR);
        }
        if (modernPaper) {
            return ItemStack.deserializeBytes(data);
        }
        try {
            Object compound;
            if (nbtAccounterUnlimitedHeap == null) {
                compound = nbtCompressedStreamToolsRead.invoke(null, new ByteArrayInputStream(data));
            } else {
                compound = nbtCompressedStreamToolsRead.invoke(null, new ByteArrayInputStream(data), nbtAccounterUnlimitedHeap.invoke(null));
            }

            if (hasDataVersion) {
                int version = (int) nbtTagCompoundGetInt.invoke(compound, "DataVersion");
                if (version == 0) {
                    version = OLD_DATA_VERSION;
                }
                if (version > dataVersion) {
                    throw new IllegalArgumentException("Attempting to load an item of version " + version + " but this server is version " + dataVersion);
                }
                compound = dynamicGetValue.invoke(dataFixerUpdate.invoke(dataConverterRegistryDataFixer, dataConverterTypesItemStack, dynamic.newInstance(dynamicOpsNBT, compound), version, dataVersion));
            }
            return (ItemStack) craftItemStackAsCraftMirror.invoke(null, itemStackFromCompound == null ? itemStackConstructor.newInstance(compound) : itemStackFromCompound.invoke(null, compound));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize item stack\n" + Base64.getEncoder().encodeToString(data), e);
        }
    }
}
