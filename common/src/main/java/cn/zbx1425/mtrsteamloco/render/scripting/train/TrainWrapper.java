package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.client.ClientData;
import mtr.data.*;
import mtr.path.PathData;
import net.minecraft.core.BlockPos;

import java.util.*;

@SuppressWarnings("unused")
public class TrainWrapper {

    public boolean[] doorLeftOpen;
    public boolean[] doorRightOpen;
    public Matrix4f[] lastWorldPose;

    private final TrainClient train;
    private PlatformLookupMap trainPlatforms;
    private List<PathData> trainPlatformsValidPath;

    public TrainWrapper(TrainClient train) {
        doorLeftOpen = new boolean[train.trainCars];
        doorRightOpen = new boolean[train.trainCars];
        lastWorldPose = new Matrix4f[train.trainCars];
        this.train = train;
        this.reset();
    }

    public void reset() {
        if (trainPlatformsValidPath == null || !trainPlatformsValidPath.equals(train.path)) {
            if (!train.getRouteIds().isEmpty()) {
                trainPlatforms = getTrainPlatforms();
                trainPlatformsValidPath = train.path;
            } else {
                trainPlatforms = new PlatformLookupMap();
            }
        }
    }

    private PlatformLookupMap getTrainPlatforms() {
        List<Long> routeIds = train.getRouteIds();
        DataCache dataCache = ClientData.DATA_CACHE;
        PlatformLookupMap result = new PlatformLookupMap();
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
                    Platform thisPlatform = dataCache.platformIdMap.get((thisRoute.platformIds.get(difference)).platformId);
                    String customDestination = thisRoute.getDestination(difference);
                    double distance = ((TrainAccessor)train).getDistances().get(processingPathIndex);
                    result.put(processingPathIndex, new PlatformInfo(thisRoute, thisStation, thisPlatform, lastStation, customDestination != null ? customDestination : lastStation.name, distance));
                    processingPathIndex++;
                }
            }
        }
        return result;
    }

    public PlatformInfo getPlatformRelative(int offset, boolean allowDifferentRoute) {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.idLookup.ceilingEntry(headIndex);
        if (ceilEntry == null) return null;
        int queryIndex = ceilEntry.getValue() + offset;
        if (queryIndex < 0 || queryIndex > trainPlatforms.platforms.size() - 1
                || (!allowDifferentRoute && !Objects.equals(trainPlatforms.platforms.get(queryIndex).route.id,
                trainPlatforms.platforms.get(ceilEntry.getValue()).route.id))) {
            return null;
        } else {
            return trainPlatforms.platforms.get(queryIndex);
        }
    }

    public List<PlatformInfo> getAllPlatforms() {
        return trainPlatforms.platforms;
    }

    public int getAllPlatformsNextIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.idLookup.ceilingEntry(headIndex);
        if (ceilEntry == null) return trainPlatforms.platforms.size();
        return ceilEntry.getValue();
    }

    public List<PlatformInfo> getThisRoutePlatforms() {
        int nextIndex = getAllPlatformsNextIndex();
        Map.Entry<Integer, Integer> routeBoundary = trainPlatforms.routeBoundary.floorEntry(Math.max(nextIndex - 1, 0));
        return routeBoundary == null ? List.of()
                : trainPlatforms.platforms.subList(routeBoundary.getKey(), routeBoundary.getValue() + 1);
    }

    public int getThisRoutePlatformsNextIndex() {
        int nextIndex = getAllPlatformsNextIndex();
        Integer routeBoundaryFrom = trainPlatforms.routeBoundary.floorKey(Math.max(nextIndex - 1, 0));
        return nextIndex - (routeBoundaryFrom == null ? 0 : routeBoundaryFrom);
    }

    public List<PlatformInfo> getDebugThisRoutePlatforms(int count) {
        List<PlatformInfo> result = new ArrayList<>();
        Route debugRoute = new Route(TransportMode.TRAIN);
        debugRoute.name = "调试线路|Debug Route";
        Station destinationStation = new Station();
        destinationStation.name = String.format("车站 %d|Station %d", count, count);
        for (int i = 0; i < count; i++) {
            Station currentStation = new Station();
            currentStation.name = String.format("车站 %d|Station %d", i + 1, i + 1);
            Platform currentPlatform = new Platform(TransportMode.TRAIN, BlockPos.ZERO, BlockPos.ZERO);
            currentPlatform.name = "1";
            result.add(new PlatformInfo(debugRoute, currentStation, currentPlatform, destinationStation, destinationStation.name, i * 1000));
        }
        return result;
    }

    private static class PlatformLookupMap {

        private final TreeMap<Integer, Integer> idLookup = new TreeMap<>();
        private final TreeMap<Integer, Integer> routeBoundary = new TreeMap<>();
        private final ArrayList<PlatformInfo> platforms = new ArrayList<>();

        public void put(int index, PlatformInfo platformInfo) {
            platforms.add(platformInfo);
            idLookup.put(index, platforms.size() - 1);
        }
    }

    public static class PlatformInfo {

        public Route route;
        public Station station;
        public Platform platform;
        public Station destinationStation;
        public String destinationName;
        public double distance;

        public PlatformInfo(Route route, Station station, Platform platform, Station destinationStation, String destinationName, double distance) {
            this.route = route;
            this.station = station;
            this.platform = platform;
            this.destinationStation = destinationStation;
            this.destinationName = destinationName;
            this.distance = distance;
        }
    }

    public String trainTypeId() { return train.trainId; }
    public String baseTrainType() { return train.baseTrainType; }
    public TransportMode transportMode() { return train.transportMode; }
    public int spacing() { return train.spacing; }
    public int width() { return train.width; }
    public int trainCars() { return train.trainCars; }
    public float accelerationConstant() { return train.accelerationConstant; }
    public boolean manualAllowed() { return train.isManualAllowed; }
    public int maxManualSpeed() { return train.maxManualSpeed; }
    public int manualToAutomaticTime() { return train.manualToAutomaticTime; }
    public List<PathData> path() { return train.path; }
    public double railProgress() { return train.getRailProgress(); }
    public double getRailProgress(int car) { return train.getRailProgress() - car * train.spacing; }
    public int getRailIndex(double railProgress, boolean roundDown) { return train.getIndex(railProgress, roundDown); }
    public float getRailSpeed(int railIndex) { return train.getRailSpeed(railIndex); }
    public float speed() { return train.getSpeed(); }
    public float doorValue() { return train.getDoorValue(); }
    public boolean isCurrentlyManual() { return train.isCurrentlyManual(); }
    public boolean isReversed() { return train.isReversed(); }
    public boolean isOnRoute() { return train.isOnRoute(); }

    public boolean justOpening() { return train.justOpening(); }
    public boolean justClosing(float doorCloseTime) { return train.justClosing(doorCloseTime); }
    public final boolean isDoorOpening() { return train.isDoorOpening(); }

}
