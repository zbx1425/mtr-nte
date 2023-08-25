package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.client.ClientData;
import mtr.data.*;
import mtr.path.PathData;

import java.util.*;

public class TrainExtra {

    public boolean[] doorLeftOpen;
    public boolean[] doorRightOpen;
    public Matrix4f[] lastWorldPose;
    
    private final TrainClient train;
    private StationIndexMap trainStations;
    private List<PathData> mapValidForPath;

    public TrainExtra(TrainClient train) {
        doorLeftOpen = new boolean[train.trainCars];
        doorRightOpen = new boolean[train.trainCars];
        lastWorldPose = new Matrix4f[train.trainCars];
        this.train = train;
    }

    public void reset() {
        if (mapValidForPath == null || !mapValidForPath.equals(train.path)) {
            if (train.getRouteIds().size() > 0) {
                trainStations = getTrainStations();
                mapValidForPath = train.path;
            } else {
                trainStations = new StationIndexMap();
            }
        }
    }

    private StationIndexMap getTrainStations() {
        List<Long> routeIds = train.getRouteIds();
        DataCache dataCache = ClientData.DATA_CACHE;
        StationIndexMap result = new StationIndexMap();
        if (routeIds.isEmpty()) return result;
        int sum = 0;
        int processingPathIndex = 0;
        for(int i = 0; i < routeIds.size(); ++i) {
            Route thisRoute = dataCache.routeIdMap.get(routeIds.get(i));
            Route nextRoute = i < routeIds.size() - 1 && !(dataCache.routeIdMap.get(routeIds.get(i + 1))).isHidden ? dataCache.routeIdMap.get(routeIds.get(i + 1)) : null;
            if (thisRoute != null) {
                Station lastStation = ClientData.DATA_CACHE.platformIdToStation.get(thisRoute.getLastPlatformId());
                int routeBeginOffset = sum;
                sum += thisRoute.platformIds.size();
                result.routeBoundary.put(routeBeginOffset, sum - 1);
                if (!thisRoute.platformIds.isEmpty() && nextRoute != null && !nextRoute.platformIds.isEmpty() && thisRoute.getLastPlatformId() == nextRoute.getFirstPlatformId()) {
                    --sum;
                }

                while (processingPathIndex < train.path.size()
                        && train.path.get(processingPathIndex).stopIndex - 1 < sum) {
                    if (train.path.get(processingPathIndex).dwellTime <= 0) {
                        processingPathIndex++;
                        continue;
                    }
                    int difference = train.path.get(processingPathIndex).stopIndex - 1 - routeBeginOffset;
                    Station thisStation = dataCache.platformIdToStation.get((thisRoute.platformIds.get(difference)).platformId);
                    String customDestination = thisRoute.getDestination(difference);
                    double distance = ((TrainAccessor)train).getDistances().get(processingPathIndex);
                    result.put(processingPathIndex, new StationInfo(thisRoute, thisStation, lastStation, customDestination != null ? customDestination : lastStation.name, distance));
                    processingPathIndex++;
                }
            }
        }
        return result;
    }

    public StationInfo getStationRelative(int offset, boolean allowDifferentRoute) {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainStations.idLookup.ceilingEntry(headIndex);
        if (ceilEntry == null) return null;
        int queryIndex = ceilEntry.getValue() + offset;
        if (queryIndex < 0 || queryIndex > trainStations.stations.size() - 1
                || (!allowDifferentRoute && !Objects.equals(trainStations.stations.get(queryIndex).route.id,
                trainStations.stations.get(ceilEntry.getValue()).route.id))) {
            return null;
        } else {
            return trainStations.stations.get(queryIndex);
        }
    }

    public List<StationInfo> getAllRoutesStations() {
        return trainStations.stations;
    }

    public int getAllRoutesNextStationIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainStations.idLookup.ceilingEntry(headIndex);
        if (ceilEntry == null) return trainStations.stations.size();
        return ceilEntry.getValue();
    }

    public List<StationInfo> getCurrentRouteStations() {
        int nextIndex = getAllRoutesNextStationIndex();
        Map.Entry<Integer, Integer> routeBoundary = trainStations.routeBoundary.floorEntry(Math.max(nextIndex - 1, 0));
        return routeBoundary == null ? List.of()
                : trainStations.stations.subList(routeBoundary.getKey(), routeBoundary.getValue() + 1);
    }

    public int getCurrentRouteNextStationIndex() {
        int nextIndex = getAllRoutesNextStationIndex();
        Integer routeBoundaryFrom = trainStations.routeBoundary.floorKey(Math.max(nextIndex - 1, 0));
        return nextIndex - (routeBoundaryFrom == null ? 0 : routeBoundaryFrom);
    }

    public List<StationInfo> getDebugStations(int count) {
        List<StationInfo> result = new ArrayList<>();
        Route debugRoute = new Route(TransportMode.TRAIN);
        debugRoute.name = "调试线路|Debug Route";
        Station destinationStation = new Station();
        destinationStation.name = String.format("车站 %d|Station %d", count, count);
        for (int i = 0; i < count; i++) {
            Station currentStation = new Station();
            currentStation.name = String.format("车站 %d|Station %d", i + 1, i + 1);
            result.add(new StationInfo(debugRoute, currentStation, destinationStation, destinationStation.name, i * 1000));
        }
        return result;
    }
    
    private static class StationIndexMap {

        private final TreeMap<Integer, Integer> idLookup = new TreeMap<>();
        private final TreeMap<Integer, Integer> routeBoundary = new TreeMap<>();
        private final ArrayList<StationInfo> stations = new ArrayList<>();

        public void put(int index, StationInfo stationInfo) {
            stations.add(stationInfo);
            idLookup.put(index, stations.size() - 1);
        }
    }

    public static class StationInfo {

        public Route route;
        public Station station;
        public Station destinationStation;
        public String destinationName;
        public double distance;

        public StationInfo(Route route, Station station, Station destinationStation, String destinationName, double distance) {
            this.route = route;
            this.station = station;
            this.destinationStation = destinationStation;
            this.destinationName = destinationName;
            this.distance = distance;
        }
    }
}
