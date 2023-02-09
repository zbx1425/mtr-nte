package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.client.ClientData;
import mtr.data.IGui;
import mtr.data.Route;
import mtr.data.Station;
import mtr.data.TrainClient;
import mtr.path.PathData;
import net.minecraft.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class VariableText {

    private static final HashMap<TrainClient, StationIndexMap> stationCache = new HashMap<>();

    private final String rawContent;
    private final String variable;
    private final int offset;
    private final int member;

    public VariableText(String script) {
        if (script.startsWith("$")) {
            rawContent = null;
            if (script.contains("[")) {
                variable = StringUtils.substringBetween(script, "$", "[");
                offset = Integer.parseInt(StringUtils.substringBetween(script, "[", "]"));
            } else {
                variable = StringUtils.substringAfter(script, "$");
                offset = 0;
            }
            if (script.contains(".")) {
                switch (StringUtils.substringAfter(script, ".")) {
                    case "visible":
                        member = 1;
                        break;
                    case "cjk":
                        member = 2;
                        break;
                    case "eng":
                        member = 3;
                        break;
                    case "extra":
                        member = 4;
                        break;
                    default:
                        member = 0;
                        break;
                }
            } else {
                member = 0;
            }
        } else {
            rawContent = script;
            variable = null;
            offset = 0;
            member = 0;
        }
    }

    public String getTargetString(TrainClient train) {
        if (variable == null) {
            return rawContent;
        } else {
            String variableResult;
            switch (variable) {
                case "route":
                    String routeName = getRelativeRoute(train, offset);
                    variableResult = routeName == null ? "" : routeName;
                    break;
                case "sta":
                    String staName = getRelativeStation(train, offset);
                    variableResult = staName == null ? "" : staName;
                    break;
                default:
                    variableResult = "";
                    break;
            }
            switch (member) {
                case 1:
                    return Util.memoize(VariableText::getExtraMatching).apply(variableResult, false);
                case 2:
                    return Util.memoize(VariableText::getCjkMatching).apply(variableResult, true);
                case 3:
                    return Util.memoize(VariableText::getCjkMatching).apply(variableResult, false);
                case 4:
                    return Util.memoize(VariableText::getExtraMatching).apply(variableResult, true);
                default:
                    return variableResult;
            }
        }
    }

    public static StationIndexMap getTrainStations(TrainClient train) {
        stationCache.keySet().removeIf(trainClient -> trainClient.isRemoved);
        if (stationCache.containsKey(train)) return stationCache.get(train);

        int currentRouteSeq = 0;
        int stopIndexOffset = 0;
        if (train.getRouteIds().size() == 0) return null;
        Route currentRoute = ClientData.DATA_CACHE.routeIdMap.get(train.getRouteIds().get(currentRouteSeq));
        if (currentRoute == null) return null;
        StationIndexMap result = new StationIndexMap();
        for (int i = 0; i < train.path.size(); i++) {
            PathData pathPiece = train.path.get(i);
            if (pathPiece.dwellTime > 0) {
                int stopIndex = pathPiece.stopIndex - 1;
                while (stopIndex > stopIndexOffset + currentRoute.platformIds.size() - 1) {
                    currentRouteSeq++;
                    stopIndexOffset += currentRoute.platformIds.size();
                    if (currentRouteSeq > train.getRouteIds().size() - 1) {
                        Main.LOGGER.error("ROUTE OVERFLOW! Requested " + stopIndex + ", satisfiable " + stopIndexOffset);
                        return null;
                    }
                    currentRoute = ClientData.DATA_CACHE.routeIdMap.get(train.getRouteIds().get(currentRouteSeq));
                    if (currentRoute == null) return null;
                }
                while (stopIndex < stopIndexOffset) {
                    currentRouteSeq--;
                    stopIndexOffset -= currentRoute.platformIds.size();
                    currentRoute = ClientData.DATA_CACHE.routeIdMap.get(train.getRouteIds().get(currentRouteSeq));
                    if (currentRoute == null) return null;
                }
                Station station = ClientData.DATA_CACHE.platformIdToStation.get(currentRoute.platformIds.get(stopIndex - stopIndexOffset).platformId);
                result.put(i, currentRoute.name, station.name);
            }
        }
        stationCache.put(train, result);
        return result;
    }

    public static String getRelativeStation(TrainClient train, int offset) {
        int headIndex = train.getIndex(0, train.spacing, true);
        StationIndexMap trainStations = getTrainStations(train);
        if (trainStations == null) return null;
        int queryIndex = trainStations.idLookup.ceilingEntry(headIndex).getValue() + offset;
        if (offset < 0 || offset > trainStations.stationNames.size() - 1) {
            return null;
        } else {
            return trainStations.stationNames.get(queryIndex);
        }
    }

    public static String getRelativeRoute(TrainClient train, int offset) {
        int headIndex = train.getIndex(0, train.spacing, true);
        StationIndexMap trainStations = getTrainStations(train);
        if (trainStations == null) return null;
        int queryIndex = trainStations.idLookup.ceilingEntry(headIndex).getValue() + offset;
        if (offset < 0 || offset > trainStations.routeNames.size() - 1) {
            return null;
        } else {
            return trainStations.routeNames.get(queryIndex);
        }
    }

    private static String getExtraMatching(String src, boolean extra) {
        if (src.contains("||")) {
            return src.split("\\|\\|", 2)[extra ? 1 : 0];
        } else {
            return "";
        }
    }

    private static String getCjkMatching(String src, boolean isCJK) {
        if (src.contains("||")) src = src.split("\\|\\|", 2)[0];
        String[] stringSplit = src.split("\\|");
        StringBuilder result = new StringBuilder();

        for (final String stringSplitPart : stringSplit) {
            if (result.length() > 0) result.append('|');
            if (IGui.isCjk(stringSplitPart) == isCJK) {
                result.append(stringSplitPart);
            }
        }
        return result.toString();
    }

    private static class StationIndexMap {

        private final TreeMap<Integer, Integer> idLookup = new TreeMap<>();
        private final ArrayList<String> routeNames = new ArrayList<>();
        private final ArrayList<String> stationNames = new ArrayList<>();

        public void put(int index, String routeName, String stationName) {
            routeNames.add(routeName);
            stationNames.add(stationName);
            idLookup.put(index, stationNames.size() - 1);
        }
    }
}
