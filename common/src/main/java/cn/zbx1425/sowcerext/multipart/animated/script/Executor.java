package cn.zbx1425.sowcerext.multipart.animated.script;

import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Executor {

    private static final Random randomGenerator = new Random();

    static double ExecuteFunctionScript(FunctionScript Function, MultipartUpdateProp prop, double elapsedTime, int CurrentState, double lastResult) {
        int s = 0, c = 0;
        for (int i = 0; i < Function.InstructionSet.length; i++) {
            switch (Function.InstructionSet[i]) {
                // system
                case SystemHalt:
                    i = Function.InstructionSet.length;
                    break;
                case SystemConstant:
                    Function.Stack[s] = Function.Constants[c];
                    s++;
                    c++;
                    break;
                case SystemConstantArray: {
                    int n = Function.InstructionSet[i + 1].ordinal();
                    for (int j = 0; j < n; j++) {
                        Function.Stack[s + j] = Function.Constants[c + j];
                    }
                    s += n;
                    c += n;
                    i++;
                }
                break;
                case SystemValue:
                    Function.Stack[s] = lastResult;
                    s++;
                    break;
                case SystemDelta:
                    Function.Stack[s] = elapsedTime;
                    s++;
                    break;
                // stack
                case StackCopy:
                    Function.Stack[s] = Function.Stack[s - 1];
                    s++;
                    break;
                case StackSwap: {
                    double a = Function.Stack[s - 1];
                    Function.Stack[s - 1] = Function.Stack[s - 2];
                    Function.Stack[s - 2] = a;
                }
                break;
                // math
                case MathPlus:
                    Function.Stack[s - 2] += Function.Stack[s - 1];
                    s--;
                    break;
                case MathSubtract:
                    Function.Stack[s - 2] -= Function.Stack[s - 1];
                    s--;
                    break;
                case MathMinus:
                    Function.Stack[s - 1] = -Function.Stack[s - 1];
                    break;
                case MathTimes:
                    Function.Stack[s - 2] *= Function.Stack[s - 1];
                    s--;
                    break;
                case MathDivide:
                    Function.Stack[s - 2] = Function.Stack[s - 1] == 0.0 ? 0.0 : Function.Stack[s - 2] / Function.Stack[s - 1];
                    s--;
                    break;
                case MathReciprocal:
                    Function.Stack[s - 1] = Function.Stack[s - 1] == 0.0 ? 0.0 : 1.0 / Function.Stack[s - 1];
                    break;
                case MathPower: {
                    double a = Function.Stack[s - 2];
                    double b = Function.Stack[s - 1];
                    if (b == 2.0) {
                        Function.Stack[s - 2] = a * a;
                    } else if (b == 3.0) {
                        Function.Stack[s - 2] = a * a * a;
                    } else if (b == 4.0) {
                        double t = a * a;
                        Function.Stack[s - 2] = t * t;
                    } else if (b == 5.0) {
                        double t = a * a;
                        Function.Stack[s - 2] = t * t * a;
                    } else if (b == 6.0) {
                        double t = a * a * a;
                        Function.Stack[s - 2] = t * t;
                    } else if (b == 7.0) {
                        double t = a * a * a;
                        Function.Stack[s - 2] = t * t * a;
                    } else if (b == 8.0) {
                        double t = a * a;
                        t *= t;
                        Function.Stack[s - 2] = t * t;
                    } else if (b == 0.0) {
                        Function.Stack[s - 2] = 1.0;
                    } else if (b < 0.0) {
                        Function.Stack[s - 2] = 0.0;
                    } else {
                        Function.Stack[s - 2] = Math.pow(a, b);
                    }
                    s--;
                    break;
                }
                case MathRandom: {
                    //Generates a random number between two given doubles
                    double min = Function.Stack[s - 2];
                    double max = Function.Stack[s - 1];
                    Function.Stack[s - 2] = min + randomGenerator.nextDouble() * (max - min);
                    s--;
                }
                break;
                case MathRandomInt: {
                    //Generates a random number between two given doubles
                    int min = (int) Function.Stack[s - 2];
                    int max = (int) Function.Stack[s - 1];
                    Function.Stack[s - 2] = ThreadLocalRandom.current().nextInt(max) + min;
                    s--;
                }
                break;
                case MathIncrement:
                    Function.Stack[s - 1] += 1.0;
                    break;
                case MathDecrement:
                    Function.Stack[s - 1] -= 1.0;
                    break;
                case MathFusedMultiplyAdd:
                    Function.Stack[s - 3] = Function.Stack[s - 3] * Function.Stack[s - 2] + Function.Stack[s - 1];
                    s -= 2;
                    break;
                case MathQuotient:
                    Function.Stack[s - 2] = Function.Stack[s - 1] == 0.0 ? 0.0 : Math.floor(Function.Stack[s - 2] / Function.Stack[s - 1]);
                    s--;
                    break;
                case MathMod:
                    Function.Stack[s - 2] = Function.Stack[s - 1] == 0.0 ? 0.0 : Function.Stack[s - 2] - Function.Stack[s - 1] * Math.floor(Function.Stack[s - 2] / Function.Stack[s - 1]);
                    s--;
                    break;
                case MathFloor:
                    Function.Stack[s - 1] = Math.floor(Function.Stack[s - 1]);
                    break;
                case MathCeiling:
                    Function.Stack[s - 1] = Math.ceil(Function.Stack[s - 1]);
                    break;
                case MathRound:
                    Function.Stack[s - 1] = Math.round(Function.Stack[s - 1]);
                    break;
                case MathMin:
                    Function.Stack[s - 2] = Function.Stack[s - 2] < Function.Stack[s - 1] ? Function.Stack[s - 2] : Function.Stack[s - 1];
                    s--;
                    break;
                case MathMax:
                    Function.Stack[s - 2] = Function.Stack[s - 2] > Function.Stack[s - 1] ? Function.Stack[s - 2] : Function.Stack[s - 1];
                    s--;
                    break;
                case MathAbs:
                    Function.Stack[s - 1] = Math.abs(Function.Stack[s - 1]);
                    break;
                case MathSign:
                    Function.Stack[s - 1] = Math.signum(Function.Stack[s - 1]);
                    break;
                case MathExp:
                    Function.Stack[s - 1] = Math.exp(Function.Stack[s - 1]);
                    break;
                case MathLog:
                    Function.Stack[s - 1] = Log(Function.Stack[s - 1]);
                    break;
                case MathSqrt:
                    Function.Stack[s - 1] = Sqrt(Function.Stack[s - 1]);
                    break;
                case MathSin:
                    Function.Stack[s - 1] = Math.sin(Function.Stack[s - 1]);
                    break;
                case MathCos:
                    Function.Stack[s - 1] = Math.cos(Function.Stack[s - 1]);
                    break;
                case MathTan:
                    Function.Stack[s - 1] = Tan(Function.Stack[s - 1]);
                    break;
                case MathArcTan:
                    Function.Stack[s - 1] = Math.atan(Function.Stack[s - 1]);
                    break;
                case MathPi:
                    Function.Stack[s] = Math.PI;
                    s++;
                    break;
                // comparisons
                case CompareEqual:
                    Function.Stack[s - 2] = Function.Stack[s - 2] == Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareUnequal:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareLess:
                    Function.Stack[s - 2] = Function.Stack[s - 2] < Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareGreater:
                    Function.Stack[s - 2] = Function.Stack[s - 2] > Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareLessEqual:
                    Function.Stack[s - 2] = Function.Stack[s - 2] <= Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareGreaterEqual:
                    Function.Stack[s - 2] = Function.Stack[s - 2] >= Function.Stack[s - 1] ? 1.0 : 0.0;
                    s--;
                    break;
                case CompareConditional:
                    Function.Stack[s - 3] = Function.Stack[s - 3] != 0.0 ? Function.Stack[s - 2] : Function.Stack[s - 1];
                    s -= 2;
                    break;
                // logical
                case LogicalNot:
                    Function.Stack[s - 1] = Function.Stack[s - 1] != 0.0 ? 0.0 : 1.0;
                    break;
                case LogicalAnd:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != 0.0 & Function.Stack[s - 1] != 0.0 ? 1.0 : 0.0;
                    s--;
                    break;
                case LogicalOr:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != 0.0 | Function.Stack[s - 1] != 0.0 ? 1.0 : 0.0;
                    s--;
                    break;
                case LogicalNand:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != 0.0 & Function.Stack[s - 1] != 0.0 ? 0.0 : 1.0;
                    s--;
                    break;
                case LogicalNor:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != 0.0 | Function.Stack[s - 1] != 0.0 ? 0.0 : 1.0;
                    s--;
                    break;
                case LogicalXor:
                    Function.Stack[s - 2] = Function.Stack[s - 2] != 0.0 ^ Function.Stack[s - 1] != 0.0 ? 1.0 : 0.0;
                    s--;
                    break;
                case CurrentObjectState:
                    Function.Stack[s] = CurrentState;
                    s++;
                    break;
                // time/camera
                // Zbx1425: System time is used instead of in-game time.
                //   Because BVE trains expect time to pass 1 sec per real world sec.
                case TimeSecondsSinceMidnight:
                    Function.Stack[s] = prop.systemTimeSecMidnight;
                    s++;
                    break;
                case TimeHourDigit:
                    Function.Stack[s] = Math.floor(prop.systemTimeSecMidnight / 3600.0);
                    s++;
                    break;
                case TimeMinuteDigit:
                    Function.Stack[s] = Math.floor(prop.systemTimeSecMidnight / 60 % 60);
                    s++;
                    break;
                case TimeSecondDigit:
                    Function.Stack[s] = Math.floor(prop.systemTimeSecMidnight % 60);
                    s++;
                    break;
                case CameraDistance:
                case CameraXDistance:
                case CameraYDistance:
                case CameraZDistance:
                case CameraView:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                // train
                case PlayerTrain:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case TrainCars:
                    Function.Stack[s] = prop.trainCars;
                    s++;
                    break;
                case TrainDestination:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case TrainSpeed:
                    Function.Stack[s] = prop.speed;
                    s++;
                    break;
                case TrainSpeedOfCar:
                    Function.Stack[s - 1] = prop.speed;
                    break;
                case TrainSpeedometer:
                    Function.Stack[s] = prop.speed;
                    s++;
                    break;
                case TrainSpeedometerOfCar:
                    Function.Stack[s - 1] = prop.speed;
                    break;
                case TrainAcceleration:
                    Function.Stack[s] = prop.acceleration;
                    s++;
                    break;
                case TrainAccelerationOfCar:
                    Function.Stack[s - 1] = prop.acceleration;
                    break;
                case TrainAccelerationMotor:
                    // TODO: More precise? Nobody really uses this variable though
                    Function.Stack[s] = prop.acceleration;
                    s++;
                    break;
                case TrainAccelerationMotorOfCar:
                    Function.Stack[s - 1] = prop.acceleration;
                    break;
                case TrainDistance:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case TrainDistanceToCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case TrainTrackDistance:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case CurveRadius:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    // TODO: Check the code in OpenBVE, what is s++ for really confuses me, these ones don't get s++
                    Function.Stack[s] = 0.0; // One [s] and one [s - 1], checking on Train == null
                    break;
                case CurveRadiusOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case FrontAxleCurveRadius:
                    Function.Stack[s] = 0.0;
                    break;
                case FrontAxleCurveRadiusOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case RearAxleCurveRadius:
                    Function.Stack[s] = 0.0;
                    break;
                case RearAxleCurveRadiusOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case CurveCant:
                    Function.Stack[s] = 0.0;
                    break;
                case CurveCantOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case Pitch:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    // One [s] and one [s - 1], checking on Train == null
                    Function.Stack[s] = 0.0;
                    break;
                case PitchOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case Odometer:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case OdometerOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case TrainTrackDistanceToCar:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s - 1] = 0.0;
                    break;
                // door
                case Doors:
                    Function.Stack[s] = Math.max(prop.leftDoorState, prop.rightDoorState);
                    s++;
                    break;
                case DoorsIndex:
                    Function.Stack[s - 1] = Math.max(prop.leftDoorState, prop.rightDoorState);
                    break;
                case LeftDoors:
                    Function.Stack[s] = prop.leftDoorState;
                    s++;
                    break;
                case LeftDoorsIndex:
                    Function.Stack[s - 1] = prop.leftDoorState;
                    break;
                case RightDoors:
                    Function.Stack[s] = prop.rightDoorState;
                    s++;
                    break;
                case RightDoorsIndex:
                    Function.Stack[s - 1] = prop.rightDoorState;
                    break;
                case LeftDoorsTarget:
                    Function.Stack[s] = prop.leftDoorTarget;
                    s++;
                    break;
                case LeftDoorsTargetIndex:
                    Function.Stack[s - 1] = prop.leftDoorTarget;
                    break;
                case RightDoorsTarget:
                    Function.Stack[s] = prop.rightDoorTarget;
                    s++;
                    break;
                case RightDoorsTargetIndex:
                    Function.Stack[s - 1] = prop.rightDoorTarget;
                    break;
                case LeftDoorButton:
                    // TODO: Can simulate, seldom used variable though
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case RightDoorButton:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case PilotLamp:
                    Function.Stack[s] = (prop.leftDoorState == 0.0 && prop.rightDoorState == 0.0) ? 1 : 0;
                    s++;
                    break;
                case PassAlarm:
                    // TODO: Can simulate, seldom used variable though
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case StationAdjustAlarm:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                // handles
                case ReverserNotch:
                    // TODO: Can simulate, variable often used in 3d cab
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case PowerNotch:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case PowerNotches:
                    Function.Stack[s] = 5.0;
                    s++;
                    break;
                case LocoBrakeNotch:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case LocoBrakeNotches:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeNotch:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeNotches:
                    Function.Stack[s] = 7.0;
                    s++;
                    break;
                case BrakeNotchLinear:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeNotchesLinear:
                    Function.Stack[s] = 7.0;
                    s++;
                    break;
                case EmergencyBrake:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case Klaxon:
                    // TODO: Future?
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case PrimaryKlaxon:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case SecondaryKlaxon:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case MusicKlaxon:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case HasAirBrake:
                    Function.Stack[s] = 1.0;
                    s++;
                    break;
                case HoldBrake:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case HasHoldBrake:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case ConstSpeed:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case HasConstSpeed:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                // brake
                case BrakeMainReservoir:
                    // TODO: Can simulate, variable often used in 3d cab
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeMainReservoirOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case BrakeEqualizingReservoir:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeEqualizingReservoirOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case BrakeBrakePipe:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeBrakePipeOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case BrakeBrakeCylinder:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeBrakeCylinderOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case BrakeStraightAirPipe:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrakeStraightAirPipeOfCar:
                    Function.Stack[s - 1] = 0.0;
                    break;
                // safety
                case SafetyPluginAvailable:
                    Function.Stack[s] = 1.0;
                    s++;
                    break;
                case SafetyPluginState:
                    int n = (int) Math.round(Function.Stack[s - 1]);
                    if (prop.pluginState != null) {
                        if (n >= 0 & n < prop.pluginState.length) {
                            Function.Stack[s - 1] = (double) prop.pluginState[n];
                        } else {
                            Function.Stack[s - 1] = 0.0;
                        }
                    } else {
                        Function.Stack[s - 1] = 0.0;
                    }
                    break;
                // timetable
                case TimetableVisible:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case Panel2Timetable:
                    //Internal instruction used to show / hide custom timetable overlay on Panel2 trains
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case DistanceNextStation:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case StopsNextStation:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case DistanceStation:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case StopsStation:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case NextStation:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case NextStationStop:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case TerminalStation:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case RouteLimit:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                // sections
                case SectionAspectNumber:
                    // TODO: Can simulate, seldom used variable though
                    Function.Stack[s] = 0;
                    s++;
                    break;
                case RainDrop:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case SnowFlake:
                    Function.Stack[s - 1] = 0.0;
                    break;
                case WiperPosition:
                    Function.Stack[s] = 0.0;
                    s++;
                    break;
                case BrightnessOfCar:
                    // TODO: Obtainable; Train authors rarely use this variable so might not be worth it
                    Function.Stack[s - 1] = 0.0;
                    break;
                case TrainCarNumber:
                    Function.Stack[s] = prop.carIndex;
                    s++;
                    break;
                // default
                default:
                    throw new IllegalArgumentException("The unknown instruction " + Function.InstructionSet[i].toString() + " was encountered in ExecuteFunctionScript.");
            }
        }
        return Function.Stack[s - 1];
    }

    // mathematical functions
    private static double Log(double X) {
        if (X <= 0.0) {
            //If X is less than or equal to 0.0 Log will return ComplexInfinity/ NonReal
            //Therefore, return 0.0
            return 0.0;
        } else {
            return Math.log(X);
        }
    }

    private static double Sqrt(double X) {
        if (X < 0.0) {
            //If X is less than or equal to 0.0 Sqrt will return NonReal
            //Therefore, return 0.0
            return 0.0;
        } else {
            return Math.sqrt(X);
        }
    }

    private static double Tan(double X) {
        double c = X / Math.PI;
        double d = c - Math.floor(c) - 0.5;
        double e = Math.floor(X >= 0.0 ? X : -X) * 1.38462643383279E-16;
        if (d >= -e & d <= e) {
            //If X is less than or equal to 0.0 Tan will return NonReal
            //Therefore, return 0.0
            return 0.0;
        } else {
            return Math.tan(X);
        }
    }

}
