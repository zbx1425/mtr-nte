package cn.zbx1425.mtrsteamloco.sound;

import mtr.MTRClient;
import mtr.data.TrainClient;
import mtr.sound.TrainSoundBase;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class DwellTimeBveTrainSound extends TrainSoundBase {

    private BveTrainSound bveTrainSound;
    private TrainClient train;

    public DwellTimeBveTrainSound(BveTrainSoundConfig config) {
        this.bveTrainSound = new BveTrainSound(config);
    }

    private DwellTimeBveTrainSound() {

    }

    @Override
    public TrainSoundBase createTrainInstance(TrainClient train) {
        DwellTimeBveTrainSound result = new DwellTimeBveTrainSound();
        result.bveTrainSound = (BveTrainSound)this.bveTrainSound.createTrainInstance(train);
        result.train = train;
        return result;
    }

    @Override
    public void playNearestCar(Level level, BlockPos blockPos, int i) {
        bveTrainSound.playNearestCar(level, blockPos, i);
    }

    @Override
    public void playAllCars(Level level, BlockPos blockPos, int i) {
        bveTrainSound.playAllCars(level, blockPos, i);
    }

    public float oldStopTicks = 0;

    private float elapsedDwellTicks = 0;
    private float totalDwellTicks = 0;
    private float lastRenderedTick = 0;

    @Override
    public void playAllCarsDoorOpening(Level world, BlockPos pos, int carIndex) {
        if (!(world instanceof ClientLevel) || train == null) {
            return;
        }

        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        final float ticksElapsed = Minecraft.getInstance().isPaused() || lastRenderedTick == MTRClient.getGameTick() ? 0 : lastFrameDuration;
        lastRenderedTick = MTRClient.getGameTick();
        elapsedDwellTicks += ticksElapsed;
        if (train.justOpening()) {
            elapsedDwellTicks = 0;
            totalDwellTicks = train.getTotalDwellTicks();
        }

        // Get door delay of the first sec off
        final int dwellTicks = (int) (totalDwellTicks - 20);
        final float stopTicks = elapsedDwellTicks;

        final SoundEvent soundEvent;
        if (train.justOpening() && bveTrainSound.config.soundCfg.doorOpen != null) {
            soundEvent = bveTrainSound.config.soundCfg.doorOpen;
        } else if (bveTrainSound.config.soundCfg.doorClose != null
            && oldStopTicks <= dwellTicks - bveTrainSound.config.soundCfg.doorCloseSoundLength * 20
            && stopTicks > dwellTicks - bveTrainSound.config.soundCfg.doorCloseSoundLength * 20) {
            soundEvent = bveTrainSound.config.soundCfg.doorClose;
        } else {
            soundEvent = null;
        }

        oldStopTicks = stopTicks;

        playLocalSound(world, soundEvent, pos);
    }

    private static void playLocalSound(Level world, SoundEvent event, BlockPos pos) {
        if (event == null) {
            return;
        }
        ((ClientLevel) world).playLocalSound(pos, event, SoundSource.BLOCKS, 1, 1, false);
    }
}
