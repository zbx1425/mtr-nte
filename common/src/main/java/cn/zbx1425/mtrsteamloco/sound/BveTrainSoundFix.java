package cn.zbx1425.mtrsteamloco.sound;

import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import mtr.data.TrainClient;
import mtr.sound.TrainSoundBase;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class BveTrainSoundFix extends TrainSoundBase {

    private BveTrainSound bveTrainSound;
    private TrainClient train;

    public BveTrainSoundFix(BveTrainSoundConfig config) {
        this.bveTrainSound = new BveTrainSound(config);
    }

    private BveTrainSoundFix() {

    }

    @Override
    public TrainSoundBase createTrainInstance(TrainClient train) {
        BveTrainSoundFix result = new BveTrainSoundFix();
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

    @Override
    public void playAllCarsDoorOpening(Level world, BlockPos pos, int carIndex) {
        if (!(world instanceof ClientLevel) || train == null) {
            return;
        }

        TrainAccessor trainAccessor = (TrainAccessor) train;
        // Get door delay of the first sec off
        final int dwellTicks = trainAccessor.getPath().get(trainAccessor.getNextStoppingIndex()).dwellTime * 10 - 20;
        final float stopTicks = trainAccessor.getStopCounter() - 20;

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
