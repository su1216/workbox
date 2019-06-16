package com.su.workbox.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.List;

public class SensorUtil {

    private static SparseArray<String> usefulSensor = new SparseArray<>();

    static {
        usefulSensor.put(Sensor.TYPE_ACCELEROMETER, "加速度传感器");
        usefulSensor.put(Sensor.TYPE_MAGNETIC_FIELD, "磁力传感器");
        usefulSensor.put(Sensor.TYPE_GYROSCOPE, "陀螺仪");
        usefulSensor.put(Sensor.TYPE_LIGHT, "光传感器");
        usefulSensor.put(Sensor.TYPE_PRESSURE, "压力传感器");
        usefulSensor.put(Sensor.TYPE_PROXIMITY, "距离传感器");
        usefulSensor.put(Sensor.TYPE_GRAVITY, "重力传感器");
        usefulSensor.put(Sensor.TYPE_LINEAR_ACCELERATION, "线性加速度");
        usefulSensor.put(Sensor.TYPE_RELATIVE_HUMIDITY, "湿度传感器");
        usefulSensor.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "温度传感器");
        usefulSensor.put(Sensor.TYPE_HEART_RATE, "心率传感器");
    }

    @NonNull
    public static String getReadableType(int type) {
        return usefulSensor.get(type);
    }

    public static boolean isUsefulSensor(int type) {
        return usefulSensor.indexOfKey(type) >= 0;
    }

    public static boolean hasUsefulSensors() {
        List<Sensor> sensors = getAllSensors();
        if (sensors == null || sensors.isEmpty()) {
            return false;
        }
        for (Sensor sensor : sensors) {
            if (isUsefulSensor(sensor.getType())) {
                return true;
            }
        }
        return false;
    }

    public static List<Sensor> getAllSensors() {
        SensorManager sensorManager = (SensorManager) GeneralInfoHelper.getContext().getSystemService(Context.SENSOR_SERVICE);
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }
}
