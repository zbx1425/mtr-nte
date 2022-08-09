package cn.zbx1425.mtrsteamloco;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerConfig {

    private static Path path;
    public static String visitorApiBaseUrl = "https://api.zbx1425.cn/mc/teacon2022";
    public static String visitorApiPrivateKeyStr = "";
    public static PrivateKey visitorApiPrivateKey;

    public static void load(Path path) {
        ServerConfig.path = path;
        try {
            JsonObject configObject = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            visitorApiBaseUrl = StringUtils.removeEnd(configObject.get("visitorApiBaseUrl").getAsString().trim(), "/");
            visitorApiPrivateKeyStr = configObject.get("visitorApiPrivateKey").getAsString().trim();

            if (!StringUtils.isEmpty(visitorApiPrivateKeyStr)) {
                byte[] keyBytes = Base64.decodeBase64(visitorApiPrivateKeyStr);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("EC");
                visitorApiPrivateKey = kf.generatePrivate(spec);
            } else {
                visitorApiPrivateKey = null;
            }
        } catch (Exception ex) {
            Main.LOGGER.warn(ex);
            save();
        }
    }

    public static void save() {
        try {
            if (path == null) return;
            JsonObject configObject = new JsonObject();
            configObject.addProperty("visitorApiBaseUrl", visitorApiBaseUrl);
            configObject.addProperty("visitorApiPrivateKey", visitorApiPrivateKeyStr);
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(configObject));
        } catch (Exception ex) {
            Main.LOGGER.warn(ex);
        }
    }

    public static void load(MinecraftServer server) {
        final Path configFilePath = server.getServerDirectory().toPath()
                .resolve("config").resolve("mtrsteamloco-server.json");
        load(configFilePath);
    }
}
