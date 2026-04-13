package dev.error;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModItemStack {
    private static final Cache<ItemStackKey, SizeResult> cache = CacheBuilder.newBuilder().maximumSize(NBTSize.CONFIG.cacheSize).build();

    public static void getTooltip(ItemStack stack, Item.TooltipContext context, List<Text> list) {
        if (!NBTSize.CONFIG.enable) return;

        SizeResult result;
        try {
            result = cache.get(new ItemStackKey(stack), () -> {
                int compressed = 0, uncompressed = 0;

                RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, context.getRegistryLookup());
                NbtElement tag = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();

                try {
                    ByteArrayOutputStream cs = new ByteArrayOutputStream();
                    NbtIo.writeCompressed((NbtCompound) tag, cs);
                    compressed = cs.size();
                } catch (IOException e) {
                    NBTSize.LOGGER.error("failed to write compressed tag: {}", e);
                }

                if (NBTSize.CONFIG.showUncompressed) {
                    try {
                        ByteArrayOutputStream us = new ByteArrayOutputStream();
                        NbtIo.write((NbtCompound) tag, new DataOutputStream(us));
                        uncompressed = us.size();
                    } catch (IOException e) {
                        NBTSize.LOGGER.error("failed to write uncompressed tag: {}", e);
                    }
                }

                return new SizeResult(compressed, uncompressed);
            });
        } catch (Exception e) {
            NBTSize.LOGGER.error("cache execution failed: {}", e);
            return;
        }

        Formatting formatting = Formatting.DARK_GRAY;
        if (result.compressed() > 0)
            list.add(Text.literal(String.format(NBTSize.CONFIG.compressedFormat, result.compressed())).formatted(formatting));
        if (NBTSize.CONFIG.showUncompressed && result.uncompressed() > 0)
            list.add(Text.literal(String.format(NBTSize.CONFIG.uncompressedFormat, result.uncompressed())).formatted(formatting));
    }

    private record ItemStackKey(ItemStack stack) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ItemStackKey(ItemStack otherStack) && ItemStack.areEqual(stack, otherStack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getItem(), stack.getCount(), stack.getComponents());
        }
    }

    private record SizeResult(int compressed, int uncompressed) {
    }
}