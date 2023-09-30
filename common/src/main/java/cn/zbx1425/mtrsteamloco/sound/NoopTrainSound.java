package cn.zbx1425.mtrsteamloco.sound;

import mtr.data.TrainClient;
import mtr.sound.TrainSoundBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class NoopTrainSound extends TrainSoundBase {

    public static final NoopTrainSound INSTANCE = new NoopTrainSound();

    @Override
    public TrainSoundBase createTrainInstance(TrainClient trainClient) {
        return this;
    }

    @Override
    public void playNearestCar(Level level, BlockPos blockPos, int i) {

    }

    @Override
    public void playAllCars(Level level, BlockPos blockPos, int i) {

    }

    @Override
    public void playAllCarsDoorOpening(Level level, BlockPos blockPos, int i) {

    }
}
