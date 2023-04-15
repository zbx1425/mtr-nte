package cn.zbx1425.mtrsteamloco;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mtr.mappings.Text;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DialogEntryPoint {

    private static final JsonParser JSON_PARSER = new JsonParser();

    public static void main(String[] args) {
        try {
            JsonObject paramObj = (JsonObject) JSON_PARSER.parse(new String(Base64.getDecoder().decode(args[0]), StandardCharsets.UTF_8));
            String langKey = paramObj.get("key").getAsString();
            ArrayList<String> langArgs = new ArrayList<>(paramObj.get("args").getAsJsonArray().size());
            for (int i = 0; i < paramObj.get("args").getAsJsonArray().size(); i++) {
                langArgs.add(paramObj.get("args").getAsJsonArray().get(i).getAsString());
            }

            CodeSource codeSource = DialogEntryPoint.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) return;

            HashMap<String, String> langEntries = new HashMap<>();
            ZipInputStream zip = new ZipInputStream(codeSource.getLocation().openStream());
            ZipEntry ze;
            while ((ze = zip.getNextEntry()) != null) {
                String entryName = ze.getName();
                if (entryName.startsWith("assets/mtrsteamloco/lang") && entryName.endsWith(".json")) {
                    String langName = entryName.substring(entryName.lastIndexOf('/') + 1).replace(".json", "");
                    JsonObject langObj = (JsonObject) JSON_PARSER.parse(new String(zip.readAllBytes(), StandardCharsets.UTF_8));
                    langEntries.put(langName, String.format(langObj.get(langKey).getAsString(), (Object[]) langArgs.toArray(new String[]{})));
                }
            }

            String msg = String.join("\n", Stream.concat(
                    Stream.of(langEntries.get("zh_cn")),
                    langEntries.entrySet().stream()
                            .filter(entry -> !entry.getKey().equals("zh_cn"))
                            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                            .map(Map.Entry::getValue)
            ).toList());
            JOptionPane.showMessageDialog(null,
                    msg, "NTE Startup Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.toString(), "NTE Startup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void startDialog(String key, String[] args) {
        Main.LOGGER.warn(Text.translatable(key, (Object[]) args).toString());

        JsonObject argObj = new JsonObject();
        JsonArray fmtArgsArray = new JsonArray();
        for (String arg : args) {
            fmtArgsArray.add(arg);
        }
        argObj.addProperty("key", key);
        argObj.add("args", fmtArgsArray);

        try {
            new ProcessBuilder(
                    // "cmd", "/c", "start", "",
                    getJvmPath(), "-cp",
                    getClassPaths(),
                    "cn.zbx1425.mtrsteamloco.DialogEntryPoint", Base64.getEncoder().encodeToString(argObj.toString().getBytes(StandardCharsets.UTF_8))
            ).start();
        } catch (Exception ex) {
            Main.LOGGER.warn(ex);
        }
    }

    private static String getJvmPath() {
        boolean isRunningOnWindowsPlatform = System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
        if (isRunningOnWindowsPlatform)
            return System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
        else
            return System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }

    private static String getClassPaths() throws URISyntaxException {
        boolean isRunningOnWindowsPlatform = System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
        char delimiter = isRunningOnWindowsPlatform ? ';' : ':';
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()
                + delimiter + new File(JsonObject.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
    }
}
