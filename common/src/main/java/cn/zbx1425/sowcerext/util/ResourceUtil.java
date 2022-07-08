package cn.zbx1425.sowcerext.util;

import cn.zbx1425.sowcer.batch.MaterialProp;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class ResourceUtil {

    public static String readResource(ResourceManager manager, ResourceLocation location) throws IOException {
        final List<Resource> resources = UtilitiesClient.getResources(manager, location);
        if (resources.size() < 1) {
            return "";
        }
        return IOUtils.toString(Utilities.getInputStream(resources.get(0)), StandardCharsets.UTF_8);
    }

    public static ResourceLocation resolveRelativePath(ResourceLocation base, String relative, String expectExtension) {
        String parentDirName = new File(base.getPath()).getParent();
        if (parentDirName == null) parentDirName = "";
        String texFileName = relative.toLowerCase(Locale.ROOT);
        if (expectExtension != null && !texFileName.endsWith(expectExtension)) {
            texFileName += expectExtension;
        }
        return new ResourceLocation(base.getNamespace(), Paths.get(parentDirName, texFileName).toString());
    }
}
