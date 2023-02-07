package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.sowcer.math.Vector3f;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;

import java.util.ArrayList;

public class DisplaySlot {

    public final String name;

    public final SlotFace[] faces;

    public DisplaySlot(JsonObject jsonObject) {
        name = jsonObject.get("name").getAsString();
        ArrayList<SlotFace> faces = new ArrayList<>();

        Vector3f[] offsets;
        if (jsonObject.has("offsets")) {
            JsonArray offsetArray = jsonObject.get("offsets").getAsJsonArray();
            offsets = new Vector3f[offsetArray.size()];
            for (int i = 0; i < offsetArray.size(); i++) {
                offsets[i] = parseVec3(offsetArray.get(i));
            }
        } else {
            offsets = new Vector3f[] { new Vector3f(0, 0, 0) };
        }

        for (Vector3f offset : offsets) {
            JsonArray faceArray = jsonObject.get("pos").getAsJsonArray();
            for (int i = 0; i < faceArray.size(); i++) {
                JsonArray posArray = faceArray.get(i).getAsJsonArray();
                faces.add(new SlotFace(
                    parseVec3(posArray.get(3), offset), parseVec3(posArray.get(0), offset),
                    parseVec3(posArray.get(2), offset), parseVec3(posArray.get(1), offset)
                ));
            }
        }

        this.faces = faces.toArray(SlotFace[]::new);
    }

    private Vector3f parseVec3(JsonElement jsonElement) {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        return new Vector3f(jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat(), jsonArray.get(2).getAsFloat());
    }

    private Vector3f parseVec3(JsonElement jsonElement, Vector3f offset) {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        Vector3f result =
                new Vector3f(jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat(), jsonArray.get(2).getAsFloat());
        result.add(offset);
        return result;
    }

    public static class SlotFace {

        private final Vector3f topLeft, topRight, bottomLeft, bottomRight;

        public SlotFace(Vector3f topLeft, Vector3f topRight, Vector3f bottomLeft, Vector3f bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }

        public Vector3f getPositionAt(float u, float v) {
            Vector3f top = linear(topLeft, topRight, u);
            Vector3f bottom = linear(bottomLeft, bottomRight, u);
            return linear(top, bottom, v);
        }

        private static Vector3f linear(Vector3f p0, Vector3f p1, float u) {
            return new Vector3f(
            p0.x() * (1 - u) + p1.x() * u,
            p0.y() * (1 - u) + p1.y() * u,
            p0.z() * (1 - u) + p1.z() * u
            );
        }
    }

}
