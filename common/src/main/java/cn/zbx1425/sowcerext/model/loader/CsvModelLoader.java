package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.batch.BatchProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.Vertex;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Tuple;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CsvModelLoader {

    public static Model loadModel(ResourceManager resourceManager, ResourceLocation objLocation, VertAttrMapping mapping) throws IOException {
        String rawModelData = mtr.sound.bve.BveTrainSoundConfig.readResource(resourceManager, objLocation);
        String[] rawModelLines = rawModelData.split("[\\r\\n]+");

        List<RawMesh> meshList = new ArrayList<>();
        RawMesh buildingMesh = new RawMesh(new BatchProp("rendertype_entity_cutout", null));
        for (String line : rawModelLines) {
            if (line.contains(";")) line = line.split(";", 1)[0];
            line = line.trim().toLowerCase();
            if (StringUtils.isEmpty(line)) continue;
            String[] tokens = line.split(",");
            switch (tokens[0]) {
                case "createmeshbuilder":
                    if (!buildingMesh.checkVertIndex()) throw new IndexOutOfBoundsException("Invalid vertex index in AddFace/AddFace2.");
                    if (buildingMesh.faces.size() > 0) meshList.add(buildingMesh);
                    buildingMesh = new RawMesh(new BatchProp("rendertype_entity_cutout", null));
                    break;
                case "addvertex":
                    if (tokens.length == 4) {
                        buildingMesh.vertices.add(new Vertex(
                                new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]))
                        ));
                    } else if (tokens.length == 7) {
                        buildingMesh.vertices.add(new Vertex(
                                new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])),
                                new Vector3f(Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5]), Float.parseFloat(tokens[6]))
                        ));
                    } else {
                        throw new IllegalArgumentException("Invalid AddVertex command.");
                    }
                    break;
                case "addface":
                case "addface2":
                    if (tokens.length < 4) throw new IllegalArgumentException("Invalid AddFace/AddFace2 command.");
                    int[] vertIndices = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; ++i) {
                        vertIndices[i - 1] = Integer.parseInt(tokens[i]);
                    }
                    buildingMesh.faces.addAll(Face.triangulate(vertIndices, tokens[0].equals("addface2")));
                    break;
                case "setcolor":
                case "setcolorall":
                    byte setcolorR = tokens.length >= 2 ? Byte.parseByte(tokens[1]) : 0;
                    byte setcolorG = tokens.length >= 3 ? Byte.parseByte(tokens[2]) : 0;
                    byte setcolorB = tokens.length >= 4 ? Byte.parseByte(tokens[3]) : 0;
                    byte setcolorA = tokens.length >= 5 ? Byte.parseByte(tokens[4]) : (byte)255;
                    if (tokens[0].equals("setcolorall")) {
                        for (RawMesh mesh : meshList) {
                            mesh.batchProp.attrState.color = setcolorR << 24 | setcolorG << 16 | setcolorB << 8 | setcolorA;
                        }
                    }
                    buildingMesh.batchProp.attrState.color = setcolorR << 24 | setcolorG << 16 | setcolorB << 8 | setcolorA;
                    break;
                case "loadtexture":
                    if (tokens.length < 2) throw new IllegalArgumentException("Invalid LoadTexture command.");
                    String parentDirName = new File(objLocation.getPath()).getParent();
                    if (parentDirName == null) parentDirName = "";
                    String texFileName = tokens[1].toLowerCase(Locale.ROOT);
                    if (!texFileName.endsWith(".png")) texFileName += ".png";
                    buildingMesh.batchProp.texture = new ResourceLocation(objLocation.getNamespace(), parentDirName + "/" + texFileName);
                    break;
                case "settexturecoordinates":
                    if (tokens.length < 4) throw new IllegalArgumentException("Invalid SetTextureCoordinates command.");
                    int vertId = Integer.parseInt(tokens[1]);
                    if (vertId < 0 || vertId >= buildingMesh.vertices.size()) throw new IndexOutOfBoundsException("Invalid vertex index in SetTextureCoordinates.");
                    buildingMesh.vertices.get(vertId).u = Float.parseFloat(tokens[2]);
                    buildingMesh.vertices.get(vertId).v = Float.parseFloat(tokens[3]);
                    break;
                case "generatenormals":
                    break;
                case "cube":
                case "cylinder":
                    break;
                case "translate":
                case "translateall":
                case "scale":
                case "scaleall":
                case "rotate":
                case "rotateall":
                case "shear":
                case "shearall":
                case "mirror":
                case "mirrorall":
                    break;
                case "setemissivecolor":
                case "setemissivecolorall":
                    break;
                case "setblendmode":
                case "setwrapmode":
                case "setdecaltransparentcolor":
                case "enablecrossfading":
                    Main.LOGGER.warn("CSV command that cannot and will not be supported: " + tokens[0]);
                    break;
            }

        }
        if (!buildingMesh.checkVertIndex()) throw new IndexOutOfBoundsException("Vertex index out of bound in " + objLocation);
        if (buildingMesh.faces.size() > 0) meshList.add(buildingMesh);

        HashMap<BatchProp, RawMesh> optimizedMeshes = new HashMap<>();
        for (RawMesh mesh : meshList) {
            RawMesh target = optimizedMeshes.computeIfAbsent(mesh.batchProp, RawMesh::new);
            target.append(mesh);
        }

        Model model = new Model();
        for (RawMesh mesh : optimizedMeshes.values()) {
            if (!mesh.checkVertIndex()) throw new AssertionError("Bad VertIndex before mesh distinct");
            mesh.distinct();
            if (!mesh.checkVertIndex()) throw new AssertionError("Bad VertIndex after mesh distinct");
            mesh.generateNormals();
            if (mesh.faces.size() == 0) continue;
            model.meshList.add(mesh.upload(mapping));
        }

        return model;
    }

}
