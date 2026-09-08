package dev.error;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModItemStack {
    private static final Cache<ItemStackKey, SizeResult> cache = CacheBuilder.newBuilder().maximumSize(NBTSize.CONFIG.cacheSize).build();

    public static void getTooltip(ItemStack stack, Item.TooltipContext context, List<Component> list) {
        if (!NBTSize.CONFIG.enable) return;

        SizeResult result;
        try {
            result = cache.get(new ItemStackKey(stack), () -> {
                int compressed = 0, uncompressed = 0;

                RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, context.registries());
                Tag tag = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();

                try {
                    ByteArrayOutputStream cs = new ByteArrayOutputStream();
                    NbtIo.writeCompressed((CompoundTag) tag, cs);
                    compressed = cs.size();
                } catch (IOException e) {
                    NBTSize.LOGGER.error("failed to write compressed tag", e);
                }

                if (NBTSize.CONFIG.showUncompressed) {
                    try {
                        ByteArrayOutputStream us = new ByteArrayOutputStream();
                        NbtIo.write((CompoundTag) tag, new DataOutputStream(us));
                        uncompressed = us.size();
                    } catch (IOException e) {
                        NBTSize.LOGGER.error("failed to write uncompressed tag", e);
                    }
                }

                return new SizeResult(compressed, uncompressed);
            });
        } catch (Exception e) {
            return;
        }

        ChatFormatting formatting = ChatFormatting.DARK_GRAY;
        if (result.compressed() > 0) try {
            list.add(Component.literal(String.format(NBTSize.CONFIG.compressedFormat, result.compressed())).withStyle(formatting));
        } catch (Exception e) {
        }
        if (NBTSize.CONFIG.showUncompressed && result.uncompressed() > 0) try {
            list.add(Component.literal(String.format(NBTSize.CONFIG.uncompressedFormat, result.uncompressed())).withStyle(formatting));
        } catch (Exception e) {
        }
    }

    private record ItemStackKey(ItemStack stack) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ItemStackKey(ItemStack otherStack) && ItemStack.matches(stack, otherStack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getItem(), stack.getCount(), stack.getComponents());
        }
    }

    private record SizeResult(int compressed, int uncompressed) {
    }
}
