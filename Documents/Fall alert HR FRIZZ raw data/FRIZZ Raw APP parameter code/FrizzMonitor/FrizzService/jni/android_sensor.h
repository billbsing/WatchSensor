#include <android/sensor.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#define AxisOfSensor 3

#define CALC_GRAV(data) (data  / 9.80665)
#define CALC_MAGN(data) (data * 0.01)

#define CALC_USEC(data) (data * 0.001 * 0.001)
#define CALC_MSEC(data) (data * 0.001)
#define CALC_DIFF(data) (data * 1000 * 1000)

#define START_STATE  0
#define STOP_STATE 1

enum
{
	X,
	Y,
	Z
};

typedef float sensor_fp;

typedef struct
{
	ASensorManager* sensorManager;

	const ASensor* gravSensor;
	const ASensor* gyroSensor;
	const ASensor* magnSensor;
	const ASensor* presSensor;

	  ASensorEventQueue* sensorEventQueue;
	  ASensorEvent event[4];
	  ALooper* looper;
}engine_t;

typedef struct
{
	sensor_fp magn[AxisOfSensor];
	sensor_fp grav[AxisOfSensor];
	sensor_fp gyro[AxisOfSensor];
	sensor_fp pres;

}sensor_android_t;

typedef struct {
	 jfloatArray magn;
	 jfloatArray grav;
	 jfloatArray gyro;
	 jfloat pres;
}sensor_java_t;

typedef struct {
  double start;
  double end;
  double diff;
  int diff_u;
  timer_t unix_time;
  int unix_time_m;
  int diff_flag;
}sensor_time_t;
