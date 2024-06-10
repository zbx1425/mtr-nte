package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.MTRClient;
import mtr.client.ClientData;
import mtr.data.*;
import mtr.path.PathData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class TrainWrapper {

    public boolean[] doorLeftOpen;
    public boolean[] doorRightOpen;

    public Matrix4f[] lastWorldPose;
    public Vector3f[] lastCarPosition;
    public Vector3f[] lastCarRotation;

    public boolean shouldRender;
    public boolean isInDetailDistance;

    private final TrainClient train;
    private PlatformLookupMap trainPlatforms;
    private List<PathData> trainPlatformsValidPath;

    public TrainWrapper(TrainClient train) {
        doorLeftOpen = new boolean[train.trainCars];
        doorRightOpen = new boolean[train.trainCars];
        lastWorldPose = new Matrix4f[train.trainCars];
        lastCarPosition = new Vector3f[train.trainCars];
        lastCarRotation = new Vector3f[train.trainCars];
        Arrays.setAll(lastWorldPose, ignored -> Matrix4f.translation(0, -10000, 0));
        Arrays.setAll(lastCarPosition, ignored -> new Vector3f(0, -10000, 0));
        Arrays.setAll(lastCarRotation, ignored -> new Vector3f(0, 0, 0));
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
        isInDetailDistance = false;
    }

    private PlatformLookupMap getTrainPlatforms() {
        List<Long> routeIds = train.getRouteIds();
        DataCache dataCache = ClientData.DATA_CACHE;
        PlatformLookupMap result = new PlatformLookupMap();
        result.siding = dataCache.sidingIdMap.get(train.sidingId);
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
                    customDestination != null ? customDestination : (lastStation != null ? lastStation.name : ""),
                    distance, reverseAtThisPlatform);

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

    @SuppressWarnings("unused")
    public List<PlatformInfo> getAllPlatforms() {
        return trainPlatforms.platforms;
    }

    @SuppressWarnings("unused")
    public int getAllPlatformsNextIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.pathToPlatformIndex.ceilingEntry(headIndex);
        if (ceilEntry == null) return trainPlatforms.platforms.size();
        return ceilEntry.getValue();
    }

    @Deprecated()
    @SuppressWarnings("unused")
    public PlatformInfo getPlatformRelative(int offset, boolean allowDifferentRoute) {
        int ceilEntry = getAllPlatformsNextIndex();
        int queryIndex = ceilEntry + offset;
        if (queryIndex < 0 || queryIndex > trainPlatforms.platforms.size() - 1
                || (!allowDifferentRoute && !Objects.equals(trainPlatforms.platforms.get(queryIndex).route.id,
                trainPlatforms.platforms.get(Math.min(ceilEntry, trainPlatforms.platforms.size() - 1)).route.id))) {
            return null;
        } else {
            return trainPlatforms.platforms.get(queryIndex);
        }
    }

    @SuppressWarnings("unused")
    public List<PlatformInfo> getThisRoutePlatforms() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, List<PlatformInfo>> ceilEntry = trainPlatforms.pathToRoutePlatforms.ceilingEntry(headIndex);
        if (ceilEntry == null) return List.of();
        return ceilEntry.getValue();
    }

    @SuppressWarnings("unused")
    public List<PlatformInfo> getNextRoutePlatforms() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, List<PlatformInfo>> ceilEntry = trainPlatforms.pathToRoutePlatforms.higherEntry(headIndex);
        if (ceilEntry == null) return List.of();
        return ceilEntry.getValue();
    }

    @SuppressWarnings("unused")
    public int getThisRoutePlatformsNextIndex() {
        int headIndex = train.getIndex(0, train.spacing, true);
        Map.Entry<Integer, Integer> ceilEntry = trainPlatforms.pathToRoutePlatformIndex.ceilingEntry(headIndex);
        if (ceilEntry == null) return getThisRoutePlatforms().size();
        return ceilEntry.getValue();
    }

    @SuppressWarnings("unused")
    public List<PlatformInfo> getDebugThisRoutePlatforms(int count) {
        List<PlatformInfo> result = new ArrayList<>();
        Route debugRoute = new Route(TransportMode.TRAIN);
        debugRoute.name = "调试线路|Debug Route||DRL";
        Station destinationStation = new Station();
        destinationStation.name = String.format("车站 %d|Station %d||S%02d", count, count, count);
        for (int i = 0; i < count; i++) {
            Station currentStation = new Station();
            currentStation.name = String.format("车站 %d|Station %d||S%02d", i + 1, i + 1, i + 1);
            Platform currentPlatform = new Platform(TransportMode.TRAIN, BlockPos.ZERO, BlockPos.ZERO);
            currentPlatform.name = "1";
            result.add(new PlatformInfo(debugRoute, currentStation, currentPlatform,
                    destinationStation, destinationStation.name, i * 1000, false));
            currentStation.exits.put("Z99", List.of(String.format("S%02d", i + 1)));
        }
        return result;
    }

    private static class PlatformLookupMap {
        public Siding siding;
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

    @SuppressWarnings("unused") public boolean shouldRender() {
        return shouldRender;
    }

    @SuppressWarnings("unused")
    public boolean shouldRenderDetail() {
        return shouldRender && (MTRClient.isReplayMod() || isInDetailDistance);
    }

    @SuppressWarnings("unused")
    public boolean isClientPlayerRiding() {
        Player player = Minecraft.getInstance().player;
        return player != null && train.isPlayerRiding(player);
    }

    @SuppressWarnings("unused") public Train mtrTrain() { return train; }
    @SuppressWarnings("unused") public long id() { return train.id; }
    @SuppressWarnings("unused") public Siding siding() { return trainPlatforms.siding; }
    @SuppressWarnings("unused") public String trainTypeId() { return train.trainId; }
    @SuppressWarnings("unused") public String baseTrainType() { return train.baseTrainType; }
    @SuppressWarnings("unused") public TransportMode transportMode() { return train.transportMode; }
    @SuppressWarnings("unused") public int spacing() { return train.spacing; }
    @SuppressWarnings("unused") public int width() { return train.width; }
    @SuppressWarnings("unused") public int trainCars() { return train.trainCars; }
    @SuppressWarnings("unused") public float accelerationConstant() { return train.accelerationConstant; }
    @SuppressWarnings("unused") public boolean manualAllowed() { return train.isManualAllowed; }
    @SuppressWarnings("unused") public int maxManualSpeed() { return train.maxManualSpeed; }
    @SuppressWarnings("unused") public int manualToAutomaticTime() { return train.manualToAutomaticTime; }
    @SuppressWarnings("unused") public List<PathData> path() { return train.path; }
    @SuppressWarnings("unused") public double railProgress() { return train.getRailProgress(); }
    @SuppressWarnings("unused") public double getRailProgress(int car) { return train.getRailProgress() - car * train.spacing; }
    @SuppressWarnings("unused") public int getRailIndex(double railProgress, boolean roundDown) { return train.getIndex(railProgress, roundDown); }
    @SuppressWarnings("unused") public float getRailSpeed(int railIndex) { return train.getRailSpeed(railIndex); }
    @SuppressWarnings("unused") public float speed() { return train.getSpeed(); }
    @SuppressWarnings("unused") public float doorValue() { return train.getDoorValue(); }
    @SuppressWarnings("unused") public boolean isCurrentlyManual() { return train.isCurrentlyManual(); }
    @SuppressWarnings("unused") public boolean isReversed() { return train.isReversed(); }
    @SuppressWarnings("unused") public boolean isOnRoute() { return train.isOnRoute(); }

    @SuppressWarnings("unused") public boolean justOpening() { return train.justOpening(); }
    @SuppressWarnings("unused") public boolean justClosing(float doorCloseTime) { return train.justClosing(doorCloseTime); }
    @SuppressWarnings("unused") public final boolean isDoorOpening() { return train.isDoorOpening(); }

}
