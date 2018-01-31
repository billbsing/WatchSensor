#include "android_sensor.h"
#include <android/log.h>
extern "C"{

int pdrStateFlag;
#define SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED     (14)
#define SENSOR_TYPE_GYROSCOPE_UNCALIBRATED          (16)
#define SENSOR_TYPE_PRESSURE						(6)

engine_t  AndroidInitEngine(int samplingTime) {

  engine_t pdr_engine;

  pdr_engine.sensorManager = ASensorManager_getInstance();

  pdr_engine.magnSensor = ASensorManager_getDefaultSensor(pdr_engine.sensorManager, SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED);
  pdr_engine.gravSensor = ASensorManager_getDefaultSensor(pdr_engine.sensorManager, ASENSOR_TYPE_ACCELEROMETER);
  pdr_engine.gyroSensor = ASensorManager_getDefaultSensor(pdr_engine.sensorManager, SENSOR_TYPE_GYROSCOPE_UNCALIBRATED );
  pdr_engine.presSensor = ASensorManager_getDefaultSensor(pdr_engine.sensorManager, SENSOR_TYPE_PRESSURE );

  pdr_engine.looper = ALooper_forThread();
  pdr_engine.looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
  pdr_engine.sensorEventQueue = ASensorManager_createEventQueue(pdr_engine.sensorManager,  pdr_engine.looper,  3, NULL, NULL);


  ASensorEventQueue_enableSensor(pdr_engine.sensorEventQueue, pdr_engine.gravSensor);
  ASensorEventQueue_setEventRate(pdr_engine.sensorEventQueue, pdr_engine.gravSensor, (1000L / (1000 / samplingTime)) * 1000);

  ASensorEventQueue_enableSensor(pdr_engine.sensorEventQueue, pdr_engine.magnSensor);
  ASensorEventQueue_setEventRate(pdr_engine.sensorEventQueue, pdr_engine.magnSensor,  (1000L / (1000 / samplingTime)) * 1000);

  ASensorEventQueue_enableSensor(pdr_engine.sensorEventQueue, pdr_engine.gyroSensor);
  ASensorEventQueue_setEventRate(pdr_engine.sensorEventQueue, pdr_engine.gyroSensor, (1000L / (1000 / samplingTime)) * 1000);

  ASensorEventQueue_enableSensor(pdr_engine.sensorEventQueue, pdr_engine.presSensor);
  ASensorEventQueue_setEventRate(pdr_engine.sensorEventQueue, pdr_engine.presSensor, (1000L / (1000 / samplingTime)) * 1000);

  return pdr_engine;
}

void AndroidSetSleep(sensor_time_t *set_time_data, int samplingTime) {
  time_t timer;
  timer = time(NULL);
  int    diff_u_time;
  double diff_time;

  struct timeval tv;
  gettimeofday(&tv, NULL);

  diff_time =  ((double)(tv.tv_sec) + (double)CALC_USEC(tv.tv_usec)) - set_time_data->start;
  diff_u_time = (samplingTime * 1000) - (int)(CALC_DIFF(diff_time));

  if(diff_u_time > 0) {
    usleep(diff_u_time);
  }

}

void AndroidGetTimeData(sensor_time_t *set_time_data) {
  time_t timer;
  timer = time(NULL);

  struct timeval tv;

  gettimeofday(&tv, NULL);

  if(set_time_data->diff_flag == 0) {
    set_time_data->start = ((double)(tv.tv_sec) + (double)CALC_USEC(tv.tv_usec));
    set_time_data->diff_flag  = 1;
  } else {
    set_time_data->end  = ((double)(tv.tv_sec) + (double)CALC_USEC(tv.tv_usec));
    set_time_data->diff = set_time_data->end - set_time_data->start;
    set_time_data->start = set_time_data->end;

  }
}

void AndroidGetSensorData(engine_t* pdr_engine, sensor_android_t *set_java_data) {

  int count;
  int i;

  while ((count = ASensorEventQueue_getEvents(pdr_engine->sensorEventQueue, pdr_engine->event, 4)) > 0) {

    for (i = 0; i < count; i++) {

      switch(pdr_engine->event[i].type) {
      case SENSOR_TYPE_GYROSCOPE_UNCALIBRATED:
          	  set_java_data->gyro[0]  = pdr_engine->event[i].vector.azimuth;
          	  set_java_data->gyro[1]  = pdr_engine->event[i].vector.pitch;
          	  set_java_data->gyro[2]  = pdr_engine->event[i].vector.roll;
          	  break;

      case ASENSOR_TYPE_ACCELEROMETER:
    	  set_java_data->grav[0] = CALC_GRAV(pdr_engine->event[i].acceleration.x);
    	  set_java_data->grav[1] = CALC_GRAV(pdr_engine->event[i].acceleration.y);
    	  set_java_data->grav[2] = CALC_GRAV(pdr_engine->event[i].acceleration.z);
    	  break;

      case SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED:
    	  set_java_data->magn[0] = CALC_MAGN(pdr_engine->event[i].magnetic.x);
    	  set_java_data->magn[1] = CALC_MAGN(pdr_engine->event[i].magnetic.y);
    	  set_java_data->magn[2] = CALC_MAGN(pdr_engine->event[i].magnetic.z);
    	  break;

      case SENSOR_TYPE_PRESSURE :
    	  set_java_data->pres    = pdr_engine->event[i].pressure;
    	  //__android_log_print(ANDROID_LOG_DEBUG, "debug","ndk pressure %f", set_java_data->pres);
    	  break;
      }
    }
  }

}

void Java_jp_megachips_frizzservice_FrizzManager_nativeSensorUpdate(JNIEnv *env, jobject thiz, jint samplingTime) {

	 sensor_android_t set_java_data;
	 sensor_java_t    send_java_data;

	 send_java_data.gyro = env->NewFloatArray(sizeof(set_java_data.gyro));
	 send_java_data.grav = env->NewFloatArray(sizeof(set_java_data.grav));
	 send_java_data.magn = env->NewFloatArray(sizeof(set_java_data.magn));

	 engine_t pdr_engine;
	 memset(&pdr_engine, 0, sizeof(engine_t));
	 pdr_engine = AndroidInitEngine(samplingTime);

	 sensor_time_t set_time_data;

	 int i = 0;
	 for(i = 0; i < 100; i++) {
	     AndroidSetSleep(&set_time_data, samplingTime);
	     AndroidGetTimeData(&set_time_data);
	     AndroidGetSensorData(&pdr_engine, &set_java_data);
	 }

	 jclass jcls  = env->GetObjectClass(thiz);
	 jmethodID sensorCallback = env->GetMethodID(jcls, "sensorCallback", "([F[F[FF)V");

	 pdrStateFlag = STOP_STATE;

	for(;;) {

		 AndroidSetSleep(&set_time_data, samplingTime);
		 AndroidGetTimeData(&set_time_data);
		 AndroidGetSensorData(&pdr_engine, &set_java_data);

		 if(pdrStateFlag == START_STATE) {
			 env->SetFloatArrayRegion(send_java_data.magn, 0, AxisOfSensor, set_java_data.magn);
			 env->SetFloatArrayRegion(send_java_data.grav, 0, AxisOfSensor, set_java_data.grav);
			 env->SetFloatArrayRegion(send_java_data.gyro, 0, AxisOfSensor, set_java_data.gyro);

			 send_java_data.pres = set_java_data.pres;

		     env->CallVoidMethod(thiz, sensorCallback,
		 			      send_java_data.gyro, send_java_data.grav, send_java_data.magn, send_java_data.pres);
		 }
	 }

}

void Java_jp_megachips_frizzservice_FrizzManager_nativeSensorStart(JNIEnv *env, jobject thiz) {
	pdrStateFlag = START_STATE;
}

void Java_jp_megachips_frizzservice_FrizzManager_nativeSensorStop(JNIEnv *env, jobject thiz) {
	pdrStateFlag = STOP_STATE;
}

}

/*
void AndroidGetSensorData(engine_t* pdr_engine, sensor_t *sdt) {
  frizz_fp **mes;

  mes = (sdt->mes)->data;

  int count;
  int i;

  while ((count = ASensorEventQueue_getEvents(pdr_engine->sensorEventQueue, pdr_engine->event, 3)) > 0) {

    for (i = 0; i < count; i++) {

      switch(pdr_engine->event[i].type) {

      case ASENSOR_TYPE_ACCELEROMETER:
	mes[GX][0] = CALC_ACCL(pdr_engine->event[i].acceleration.x);
	mes[GY][0] = CALC_ACCL(pdr_engine->event[i].acceleration.y);
	mes[GZ][0] = CALC_ACCL(pdr_engine->event[i].acceleration.z);

	break;

      case ASENSOR_TYPE_GYROSCOPE:
	mes[WX][0] = pdr_engine->event[i].vector.azimuth;
	mes[WY][0] = pdr_engine->event[i].vector.pitch;
	mes[WZ][0] = pdr_engine->event[i].vector.roll;

	break;

      case ASENSOR_TYPE_MAGNETIC_FIELD:
	mes[MX][0] = CALC_MAG(pdr_engine->event[i].magnetic.x);
	mes[MY][0] = CALC_MAG(pdr_engine->event[i].magnetic.y);
	mes[MZ][0] = CALC_MAG(pdr_engine->event[i].magnetic.z);

	break;
      }
    }
  }

}

void AndroidSet9AxisSensorData(sensor_android_t *set_java_data, sensor_t *sdt) {

  set_java_data->mag[X] = sdt->mes->data[MX][0];
  set_java_data->mag[Y] = sdt->mes->data[MY][0];
  set_java_data->mag[Z] = sdt->mes->data[MZ][0];

  set_java_data->accl[X] = sdt->mes->data[GX][0];
  set_java_data->accl[Y] = sdt->mes->data[GY][0];
  set_java_data->accl[Z] = sdt->mes->data[GZ][0];

  set_java_data->gyro[X] = sdt->mes->data[WX][0];
  set_java_data->gyro[Y] = sdt->mes->data[WY][0];
  set_java_data->gyro[Z] = sdt->mes->data[WZ][0];

}

void AndroidSetPositionData(sensor_android_t *set_java_data, PDR_t *PDR) {
  set_java_data->pos[X] = PDR->KF_wtrk->stv->data[vPX][0];
  set_java_data->pos[Y] = PDR->KF_wtrk->stv->data[vPY][0];
}



*/
