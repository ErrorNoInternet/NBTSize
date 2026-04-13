package dev.error;

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

public class ModItemStack {
    public static void getTooltip(ItemStack stack, Item.TooltipContext context, List<Text> list) {
        if (!NBTSize.CONFIG.enable) return;

        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, context.getRegistryLookup());
        NbtElement tag = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();

        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed((NbtCompound) tag, compressedStream);
            list.add(Text.literal(String.format(NBTSize.CONFIG.compressedFormat, compressedStream.size())).formatted(Formatting.DARK_GRAY));
        } catch (IOException exception) {
            NBTSize.LOGGER.error("failed to write compressed tag: {}", exception);
        }

        if (NBTSize.CONFIG.showUncompressed) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                NbtIo.write((NbtCompound) tag, new DataOutputStream(stream));
                list.add(Text.literal(String.format(NBTSize.CONFIG.uncompressedFormat, stream.size())).formatted(Formatting.DARK_GRAY));
            } catch (IOException exception) {
                NBTSize.LOGGER.error("failed to write uncompressed tag: {}", exception);
            }
        }
    }
}