package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.client.ClientData;
import mtr.data.DataCache;
import mtr.data.Route;
import mtr.data.Station;
import mtr.data.TrainClient;
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
            }
        }
    }

    private StationIndexMap getTrainStations() {
        List<Long> routeIds = train.getRouteIds();
        DataCache dataCache = ClientData.DATA_CACHE;
        if (routeIds.size() == 0) return null;
        StationIndexMap result = new StationIndexMap();
        int sum = 0;
        int processingPathIndex = 0;
        for(int i = 0; i < routeIds.size(); ++i) {
            Route thisRoute = dataCache.routeIdMap.get(routeIds.get(i));
            Route nextRoute = i < routeIds.size() - 1 && !(dataCache.routeIdMap.get(routeIds.get(i + 1))).isHidden ? dataCache.routeIdMap.get(routeIds.get(i + 1)) : null;
            Station lastStation = ClientData.DATA_CACHE.platformIdToStation.get(thisRoute.getLastPlatformId());
            if (thisRoute != null) {
                int routeBeginOffset = sum;
                sum += thisRoute.platformIds.size();
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

    public StationInfo getRelativeStation(int offset, boolean allowDifferentLine) {
        int headIndex = train.getIndex(0, train.spacing, true);
        if (trainStations == null) return null;
        Map.Entry<Integer, Integer> ceilEntry = trainStations.idLookup.ceilingEntry(headIndex);
        if (ceilEntry == null) return null;
        int queryIndex = ceilEntry.getValue() + offset;
        if (queryIndex < 0 || queryIndex > trainStations.stations.size() - 1
                || (!allowDifferentLine && !Objects.equals(trainStations.stations.get(queryIndex).route.id, trainStations.stations.get(ceilEntry.getValue()).route.id))) {
            return null;
        } else {
            return trainStations.stations.get(queryIndex);
        }
    }
    
    private static class StationIndexMap {

        private final TreeMap<Integer, Integer> idLookup = new TreeMap<>();
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
