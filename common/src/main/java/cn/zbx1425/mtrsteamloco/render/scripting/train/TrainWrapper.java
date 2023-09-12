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
        Arrays.setAll(lastWorldPose, ignored -> Matrix4f.translation(0, -10000, 0));
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

        int routeIndex = 0;
        List<PlatformInfo> currentRoutePlatforms = new ArrayList<>();
        for (int pathIndex = 0; pathIndex < train.path.size(); pathIndex++) {
            if (train.path.get(pathIndex).dwellTime <= 0) continue;
            if (train.path.get(pathIndex).rail.railType != RailType.PLATFORM) continue;

            if (routeIndex >= routeIds.size()) break;
            Route thisRoute = dataCache.routeIdMap.get(routeIds.get(routeIndex));
            Route nextRoute = routeIndex < routeIds.size() - 1 ? dataCache.routeIdMap.get(routeIds.get(routeIndex + 1)) : null;
            boolean reverseAtPlatform = !thisRoute.platformIds.isEmpty() && nextRoute != null && !nextRoute.platformIds.isEmpty()
                    && thisRoute.getLastPlatformId() == nextRoute.getFirstPlatformId();

            int routeStationIndex = currentRoutePlatforms.size();
            Station thisStation = dataCache.platformIdToStation.get((thisRoute.platformIds.get(routeStationIndex)).platformId);
            Platform thisPlatform = dataCache.platformIdMap.get((thisRoute.platformIds.get(routeStationIndex)).platformId);
            String customDestination = thisRoute.getDestination(routeStationIndex);
            double distance = ((TrainAccessor)train).getDistances().get(pathIndex);
            boolean reverseAtThisPlatform = (currentRoutePlatforms.size() + 1 >= thisRoute.platformIds.size() && reverseAtPlatform);
            Station lastStation = ClientData.DATA_CACHE.platformIdToStation.get(thisRoute.getLastPlatformId());
            PlatformInfo platformInfo = new PlatformInfo(thisRoute, thisStation, thisPlatform, lastStation,
                    customDestination != null ? customDestination : lastStation.name, distance, reverseAtThisPlatform);

            result.pathToPlatformIndex.put(pathIndex, result.platforms.size());
            result.platforms.add(platformInfo);
            result.pathToRoutePlatformIndex.put(pathIndex, currentRoutePlatforms.size());
            currentRoutePlatforms.add(platformInfo);

            if (currentRoutePlatforms.size() >= thisRoute.platformIds.size()) {
                result.pathToRoutePlatforms.put(pathIndex, currentRoutePlatforms);
                currentRoutePlatforms = new ArrayList<>();
                routeIndex++;
                if (reverseAtPlatform) {
                    currentRoutePlatforms.add(platformInfo);
                }
            }
        }

        return result;
    }

    public List<PlatformInfo> getAllPlatforms() {
        return trainPlatforms.platforms;
    }

    public int getAllPlatformsNextIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.pathToPlatformIndex.ceilingEntry(headIndex);
        if (ceilEntry == null) return trainPlatforms.platforms.size();
        return ceilEntry.getValue();
    }

    public List<PlatformInfo> getThisRoutePlatforms() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, List<PlatformInfo>> ceilEntry = trainPlatforms.pathToRoutePlatforms.ceilingEntry(headIndex);
        if (ceilEntry == null) return List.of();
        return ceilEntry.getValue();
    }

    public List<PlatformInfo> getNextRoutePlatforms() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, List<PlatformInfo>> ceilEntry = trainPlatforms.pathToRoutePlatforms.higherEntry(headIndex);
        if (ceilEntry == null) return List.of();
        return ceilEntry.getValue();
    }

    public int getThisRoutePlatformsNextIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.pathToRoutePlatformIndex.ceilingEntry(headIndex);
        if (ceilEntry == null) return getThisRoutePlatforms().size();
        return ceilEntry.getValue();
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
            result.add(new PlatformInfo(debugRoute, currentStation, currentPlatform,
                    destinationStation, destinationStation.name, i * 1000, false));
        }
        return result;
    }

    private static class PlatformLookupMap {
        public final List<PlatformInfo> platforms = new ArrayList<>();
        public final TreeMap<Integer, Integer> pathToPlatformIndex = new TreeMap<>();
        public final TreeMap<Integer, List<PlatformInfo>> pathToRoutePlatforms = new TreeMap<>();
        public final TreeMap<Integer, Integer> pathToRoutePlatformIndex = new TreeMap<>();
    }

    public static class PlatformInfo {

        public Route route;
        public Station station;
        public Platform platform;
        public Station destinationStation;
        public String destinationName;
        public double distance;
        public boolean reverseAtPlatform;

        public PlatformInfo(Route route, Station station, Platform platform,
                            Station destinationStation, String destinationName, double distance,
                            boolean reverseAtPlatform) {
            this.route = route;
            this.station = station;
            this.platform = platform;
            this.destinationStation = destinationStation;
            this.destinationName = destinationName;
            this.distance = distance;
            this.reverseAtPlatform = reverseAtPlatform;
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
