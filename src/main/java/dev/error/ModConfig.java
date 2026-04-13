package dev.error;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.client.resource.language.I18n;

@Config(name = NBTSize.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean enable = true;
    public boolean showUncompressed = true;
    public String compressedFormat = I18n.translate("text.nbtsize.label.compressedFormat");
    public String uncompressedFormat = I18n.translate("text.nbtsize.label.uncompressedFormat");
}