package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.ServerConfig;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RequestFactory {

    public static Request buildVisit(String counterName, Player player, BiConsumer<Boolean, Integer> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("counterName", StringUtils.abbreviate(counterName, 255));
        payload.addProperty("playerId", player.getGameProfile().getId().toString().replace("-", ""));
        payload.addProperty("playerName", StringUtils.abbreviate(player.getGameProfile().getName(), 255));
        return new Request(ServerConfig.visitorApiBaseUrl + "/visit.php", "POST", payload, elem -> {
            callback.accept(elem.get("firstVisit").getAsBoolean(), elem.get("visitorNum").getAsInt());
        });
    }

    public static Request buildVisitStat(String counterName, BiConsumer<Integer, Integer> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("counterName", counterName);
        return new Request(ServerConfig.visitorApiBaseUrl + "/visitstatbasic.php", "POST", payload, elem -> {
            callback.accept(elem.get("visits").getAsInt(), elem.get("visitors").getAsInt());
        });
    }

    public static Request buildFeedback(String counterName, Player player, String content, Consumer<String> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("counterName", StringUtils.abbreviate(counterName, 255));
        payload.addProperty("playerId", player.getGameProfile().getId().toString().replace("-", ""));
        payload.addProperty("playerName",  StringUtils.abbreviate(player.getGameProfile().getName(), 255));
        payload.addProperty("content", content);
        return new Request(ServerConfig.visitorApiBaseUrl + "/feedback.php", "POST", payload, elem -> {
            callback.accept(elem.get("viewUrl").getAsString());
        });
    }
}
