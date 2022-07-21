package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcerext.multipart.animated.AnimatedPartStates;
import mtr.data.TrainClient;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Set;

public class MultipartUpdateProp {

    public float speed;
    public float acceleration;
    public int trainCars;
    public int carIndex;

    public float systemTimeSecMidnight;

    public float leftDoorState;
    public float rightDoorState;
    public int leftDoorTarget;
    public int rightDoorTarget;

    public int[] pluginState = new int[0];

    public AnimatedPartStates animatedPartStates = new AnimatedPartStates();

    public float miKeyframeTime;
    public Set<String> miHiddenParts;

    public void update(TrainClient train, int carIndex, boolean head1IsFront) {
        this.speed = train.getSpeed() * 20F;
        this.acceleration = train.speedChange(); // TODO
        this.trainCars = train.trainCars;
        this.carIndex = head1IsFront ? carIndex : (train.trainCars - carIndex - 1);
        this.systemTimeSecMidnight = LocalTime.now().get(ChronoField.MILLI_OF_DAY) / 1000F;
    }
}
