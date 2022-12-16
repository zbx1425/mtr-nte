package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

public class NmbModelLoader {

    public static RawModel loadModel(ResourceManager resourceManager, ResourceLocation location, AtlasManager atlasManager) throws IOException {
        final List<Resource> resources = UtilitiesClient.getResources(resourceManager, location);
        if (resources.size() < 1) throw new FileNotFoundException();
        DataInputStream dis = new DataInputStream(Utilities.getInputStream(resources.get(0)));
        dis.skipNBytes(8);
        int versionMajor = dis.readInt();
        int versionMinor = dis.readInt();

        byte[] dContent;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] key = dis.readNBytes(32);
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            byte[] iv = Arrays.copyOfRange(sha256.digest(key), 0, 16);
            IvParameterSpec aesIv = new IvParameterSpec(iv);

            int len = dis.readInt();
            byte[] eContent = dis.readNBytes(len);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, aesIv);
            dContent = cipher.doFinal(eContent);
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            dis.close();
        }

        RawModel model = new RawModel(new DataInputStream(new ByteArrayInputStream(dContent)));
        model.sourceLocation = location;
        if (atlasManager != null) {
            for (RawMesh mesh : model.meshList.values()) {
                atlasManager.applyToMesh(mesh);
            }
        }

        return model;
    }

    public static void serializeModel(RawModel model, OutputStream os, boolean withRaw) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        model.serializeTo(new DataOutputStream(bos));

        byte[] eContent, key;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey().getEncoded();
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            byte[] iv = Arrays.copyOfRange(sha256.digest(key), 0, 16);
            IvParameterSpec aesIv = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, aesIv);
            eContent = cipher.doFinal(bos.toByteArray());
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        DataOutputStream dos = new DataOutputStream(os);
        dos.write("ZBXNMB10".getBytes(StandardCharsets.UTF_8));
        dos.writeInt(1);
        dos.writeInt(0);
        dos.write(key);
        dos.writeInt(eContent.length);
        dos.write(eContent);
        if (withRaw) {
            for (int i = 0; i < 16; i++) dos.writeInt(0);
            dos.write(bos.toByteArray());
        }
    }
}
