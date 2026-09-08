package dev.error;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.client.resources.language.I18n;

@Config(name = NBTSize.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean enable = true;
    public boolean showUncompressed = true;
    public int cacheSize = 64;
    public String compressedFormat = I18n.get("text.nbtsize.label.compressedFormat");
    public String uncompressedFormat = I18n.get("text.nbtsize.label.uncompressedFormat");
}
