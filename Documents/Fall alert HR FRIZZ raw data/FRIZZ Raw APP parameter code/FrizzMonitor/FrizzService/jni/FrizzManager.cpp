#include <jni.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <poll.h>
#include <pthread.h>
#include <android/log.h>
#include <poll.h>
#include <unistd.h>
#include <stdio.h>
#include "frizz_hal_if.h"

extern "C"{

#define LOG_TAG "FRIZZ JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define HUB_MGR_GEN_CMD_CODE(cmd_id, imm0, imm1, imm2)	\
	(unsigned int)( (((cmd_id)&0xFF)<<24) | (((imm0)&0xFF)<<16) | (((imm1)&0xFF)<<8) | ((imm2)&0xFF) )

static int fd;
static int poll_flag = 0;
static int start_flag = 0;
//static sensor_enable_t sensor_enable[2];

static inline int64_t timevalToNano(timeval const& t) {
	return t.tv_sec*1000000000LL + t.tv_usec*1000;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeOpen(JNIEnv *env, jobject thiz, jstring dev_name) {
	char buff[64];
	const char *char_dev_name = env->GetStringUTFChars(dev_name, 0);
	int ret=0;
    sensor_enable_t sensor_enable;

	strcpy(buff, char_dev_name);

//	fd = open(buff, O_RDWR);
	fd = open(buff, O_RDONLY);

	if(fd == -1) {
		return -1;
	}

	return 0;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeClose(JNIEnv *env, jobject thiz) {
	if (close(fd) != 0) {
		return -1;
	}
	poll_flag = 0;
	return 0;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeSetEnable(JNIEnv *env, jobject thiz,int sensors, int flag) {
	int ret;
    sensor_enable_t sensor_enable;

    sensor_enable.code = sensors;
    sensor_enable.flag = flag;

	ret=ioctl(fd, FRIZZ_IOCTL_SENSOR_SET_ENABLE, &sensor_enable);
	LOGI("nativeSetEnable set enable ret = %d type: %d flag: %d\n", ret, sensor_enable.code, sensor_enable.flag);
	return ret;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeSetDelay(JNIEnv *env, jobject thiz,int sensors, int delay_ms) {
	sensor_delay_t sensor_delay;
	sensor_delay.code = sensors;
	sensor_delay.ms   = delay_ms;

	return ioctl(fd, FRIZZ_IOCTL_SENSOR_SET_DELAY, &sensor_delay);
}


static void convert_float(unsigned int id, float *val) {
    float *fd = (float*)&id;

    *val = *fd;
}

static void convert_uint(float id, unsigned int *val) {
    unsigned int *fd = (unsigned int*)&id;

    *val = *fd;
}

JNIEXPORT jobject JNICALL Java_jp_megachips_frizzservice_FrizzManager_nativePoll(JNIEnv *env, jobject obj, jint type) 
{
	sensor_data_t sensor;
	int err;

	memset(&sensor, 0x00, sizeof(sensor_data_t));

	sensor.code = type;
	err = ioctl(fd,  FRIZZ_IOCTL_SENSOR_GET_DATA, &sensor);

	if(err < 0) 
	{
		LOGE("ork jni, read sensor event failed!");
		return NULL;
	}
	else
	{

		jclass clsevent = env->FindClass("android/hardware/SensorEvent");
		jmethodID eventMethod = env->GetMethodID(clsevent, "<init>", "(I)V");
		jfieldID e_values = env->GetFieldID(clsevent, "values", "[F");
		jfieldID e_timestamp = env->GetFieldID(clsevent, "timestamp", "J");
		jobject objevent = env->NewObject(clsevent, eventMethod, FRIZZ_MAX_SENSOR_DATA_SIZE);

		jfloatArray jarr = env->NewFloatArray(FRIZZ_MAX_SENSOR_DATA_SIZE);
		int i;

		int size = sizeof(sensor.f32_value)/sizeof(float);

		if (sensor.code == SENSOR_TYPE_PDR) 
		{
			float *sensor_f32_value = (float *)sensor.f32_value;
			float count = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &count);
			env->SetFloatArrayRegion(jarr, 1, 1, &sensor_f32_value[1]);
			env->SetFloatArrayRegion(jarr, 2, 1, &sensor_f32_value[2]);
			env->SetFloatArrayRegion(jarr, 3, 1, &sensor_f32_value[3]);
			env->SetFloatArrayRegion(jarr, 4, 1, &sensor_f32_value[4]);
			env->SetFloatArrayRegion(jarr, 5, 1, &sensor_f32_value[5]);
		} 
		else if(sensor.code == SENSOR_TYPE_MAGNET_CALIB_RAW) 
		{
			float *sensor_f32_value = (float *)sensor.f32_value;

			for(i = 0; i < 17; i++) 
			{
				env->SetFloatArrayRegion(jarr, i, 1, &sensor_f32_value[i + 1]);
			}

			float count     = (float)(sensor.f32_value[17] >> 24);
			float mag_value = (float)((sensor.f32_value[17] & 0x00ff0000) >> 16);
			float quarity   = (float)((sensor.f32_value[17] & 0x0000ff00) >> 8);
			float init      = (float)((sensor.f32_value[17] & 0x000000ff));

			env->SetFloatArrayRegion(jarr, 16, 1, &count);
			env->SetFloatArrayRegion(jarr, 17, 1, &mag_value);
			env->SetFloatArrayRegion(jarr, 18, 1, &quarity);
			env->SetFloatArrayRegion(jarr, 19, 1, &init);

			//LOGE("calib status %d %d %d %d %d\n", sensor.f32_value[17], count, mag_value, quarity, init);

		}
		else if(sensor.code==SENSOR_TYPE_GESTURE)
		{
			float value = (float)sensor.f32_value[0];
			//LOGI("gesture = %f\n", value);
			env->SetFloatArrayRegion(jarr, 0, 1, &value);			
		}
		else if(sensor.code==SENSOR_TYPE_STEP_COUNTER)
		{
			float steps = (float)sensor.f32_value[0];
			float times = (float)sensor.f32_value[1];
			env->SetFloatArrayRegion(jarr, 0, 1, &steps);			
			env->SetFloatArrayRegion(jarr, 1, 1, &times);			
		}
		else if(sensor.code==SENSOR_TYPE_BLOOD_PRESSURE)
		{
			float heartRate = 0;
			float BP_max = 0;
			float BP_min = 0;
			float BP_status =0;

			BP_status = (float) (sensor.f32_value[4] & 0x000000ff);
			if(BP_status == 2)
			{	
				heartRate = (float)(sensor.f32_value[1] & 0x0000ffff);
				BP_max = (float)((sensor.f32_value[1] & 0xffff0000)>>16);
				BP_min = (float) (sensor.f32_value[2] & 0x0000ffff);
			}
			else if(BP_status == 1)
			{
				heartRate = (float)(sensor.f32_value[1] & 0x0000ffff);
				BP_max = 0;
				BP_min = 0;
			}
			
			env->SetFloatArrayRegion(jarr, 0, 1, &heartRate);	
			env->SetFloatArrayRegion(jarr, 1, 1, &BP_max);	
			env->SetFloatArrayRegion(jarr, 2, 1, &BP_min);
			
		}
		else if(sensor.code==SENSOR_TYPE_BLOOD_PRESSURE_LEARN)
		{
			float BP_lp = (float)(sensor.f32_value[42] & 0x0000ffff);
			env->SetFloatArrayRegion(jarr, 0, 1, &BP_lp);	
		}		
		else if(sensor.code==SENSOR_TYPE_PPG_RAW)
		{
			float value = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &value);
		}
		else if(sensor.code==SENSOR_TYPE_WEARING_DETECTOR)
		{
			float value = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &value);
		}
		else if(sensor.code==SENSOR_TYPE_MOTION_DETECTOR)
		{
			float value = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &value);			
		}
		else if(sensor.code==SENSOR_TYPE_ACTIVITY_DETECTOR)
		{
			float statustime = (float)sensor.f32_value[0];
			float status = (float)sensor.f32_value[1];
			float step_cnt = (float)sensor.f32_value[2];
			float tossAndTurn_cnt = (float)sensor.f32_value[3];

			env->SetFloatArrayRegion(jarr, 0, 1, &statustime);	
			env->SetFloatArrayRegion(jarr, 1, 1, &status);	
			env->SetFloatArrayRegion(jarr, 2, 1, &step_cnt);
			env->SetFloatArrayRegion(jarr, 3, 1, &tossAndTurn_cnt);

			//LOGI("SENSOR_TYPE_ACTIVITY_DETECTOR  statustime:%f, status:%f, step_cnt:%f, tossAndTurn_cnt:%f\n", statustime, status,step_cnt,tossAndTurn_cnt);
		}
		else if(sensor.code==SENSOR_TYPE_STAIR_DETECTOR)
		{
			float status= (float)sensor.f32_value[0];
			float altitude=(*(float*)&sensor.f32_value[1]);

			env->SetFloatArrayRegion(jarr, 0, 1, &status);	
			env->SetFloatArrayRegion(jarr, 1, 1, &altitude);	
		}
		else if(sensor.code==SENSOR_TYPE_MOTION_SENSING)
		{
			float status = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &status);	
		}
		else if(sensor.code==SENSOR_TYPE_CALORIE)
		{
			float status=(*(float*)&sensor.f32_value[0]);
			
			env->SetFloatArrayRegion(jarr, 0, 1, &status);	
		}
		else if(sensor.code==SENSOR_TYPE_BIKE_DETECTOR)
		{
			float status = (float)sensor.f32_value[0];
			env->SetFloatArrayRegion(jarr, 0, 1, &status);	
		}	
		else if(sensor.code==SENSOR_TYPE_GYRO_LPF)
		{
			float X_asix = (*(float*)&sensor.f32_value[0]);
			float Y_asix = (*(float*)&sensor.f32_value[1]);
			float Z_asix = (*(float*)&sensor.f32_value[2]);
			float T_asix = (*(float*)&sensor.f32_value[3]);

			env->SetFloatArrayRegion(jarr, 0, 1, &X_asix);	
			env->SetFloatArrayRegion(jarr, 1, 1, &Y_asix);	
			env->SetFloatArrayRegion(jarr, 2, 1, &Z_asix);
			env->SetFloatArrayRegion(jarr, 3, 1, &T_asix);
		}		
		else 
		{
			float *sensor_f32_value = (float *)sensor.f32_value;
			env->SetFloatArrayRegion(jarr, 0, 32, sensor_f32_value);
		}

		env->SetObjectField(objevent, e_values, jarr);
		env->SetLongField(objevent, e_timestamp, timevalToNano(sensor.time));
		return objevent;
	}
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeGetVersion(JNIEnv *env, jobject thiz, int sensors) {
	sensor_version_t sensor_version;
	sensor_version.code = sensors;

	int status = ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_VERSION, &sensor_version);

	return sensor_version.number;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeSetFrizzCommand(JNIEnv *env, jobject thiz, int hex_sensor_id, int hex_payload, jintArray hex_data) {
	unsigned int packet_hex_data[FRIZZ_MAX_PACKET_SIZE];
	int i;
	int value;

	jint *ret = env->GetIntArrayElements(hex_data, NULL);

	packet_hex_data[0] = HUB_MGR_GEN_CMD_CODE(0xFF, 0x81, (unsigned int)(hex_sensor_id), (unsigned int)(hex_payload));
	packet_hex_data[1] = HUB_MGR_GEN_CMD_CODE(ret[0], 0x00, 0x00, 0x00);

	for(i = 1; i < hex_payload; i++) {
		packet_hex_data[i + 1] = (unsigned int)ret[i];
	}

	//confirm float data
	//float *fd = (float*)&ret[1];
	//LOGE("data %f", *fd);

	value = ioctl(fd, FRIZZ_IOCTL_SENSOR_SET_FRIZZ_COMMAND, packet_hex_data);

	env->ReleaseIntArrayElements(hex_data, ret, 0);

	return value;
}


int Java_jp_megachips_frizzservice_FrizzManager_nativeChangePdrHolding(JNIEnv *env, jobject thiz, int holding) {
	return ioctl(fd, FRIZZ_IOCTL_SENSOR_SET_PDR_HOLDING, &holding);
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeOffsetPdrPosition(JNIEnv *env, jobject thiz, float diff_x, float diff_y) {
	unsigned int *array;
	float tmp[2];
	float tmp2[2];
	tmp[0] = diff_x;
	tmp[1] = diff_y;
	array = (unsigned int*)tmp;
	/*
	convert_uint(diff_x, &array[0]);
	convert_uint(diff_y, &array[1]);
	LOGE("offset %d %d", array[0], array[1]);*/
/*
	convert_float(array[0], &tmp2[0]);
	convert_float(array[1], &tmp2[1]);
	LOGE("offset float %f %f", tmp2[0], tmp2[1]);*/
	return ioctl(fd, FRIZZ_IOCTL_SENSOR_OFFSET_PDR_POSITION, array);
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeOffsetPdrDirection(JNIEnv *env, jobject thiz, float direction) {
	unsigned int tmp;
	float tmp2;
	convert_uint(direction, &tmp);
	LOGE("offset direction uint %d", tmp);
	convert_float(tmp, &tmp2);
	LOGE("offset direction float %f", tmp2);
	return ioctl(fd, FRIZZ_IOCTL_SENSOR_OFFSET_PDR_DIRECTION, &tmp);
}

int Java_jp_megachips_frizzservice_FrizzManager_nativeGetSensorData(JNIEnv *env, jobject thiz,int sensors) {
	sensor_data_t sensor_data;
	jfloatArray send_data;
	jlong time_sec;
	jlong time_usec;

	send_data = env->NewFloatArray(sizeof(sensor_data.f32_value));

	sensor_data.code = sensors;
	ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_DATA, &sensor_data);

	//env->SetFloatArrayRegion(send_data, 0, 6, sensor_data.f32_value);
	time_sec = sensor_data.time.tv_sec;
	time_usec = sensor_data.time.tv_usec;

	jclass jcls = env->GetObjectClass(thiz);
	jmethodID sensorCallback = env->GetMethodID(jcls, "callbackSensorDataFromNdk", "(IJJ[F)V");

	env->CallVoidMethod(thiz, sensorCallback, sensor_data.code, time_sec, time_usec, send_data);

	return 0;
}

void Java_jp_megachips_frizzservice_FrizzManager_nativeCloseSensorData(JNIEnv *env, jobject thiz) {
	start_flag = 1;
}

int Java_jp_megachips_frizzservice_FrizzManager_nativePollSensorData(JNIEnv *env, jobject thiz, jstring file_name, int sleep_usec) {

	sensor_data_t gyro, accl, mag, press;

	jfloatArray java_gyro, java_accl, java_mag;
	jfloat java_press;
	jfloat f_value[6];

	FILE *fp;

	struct timeval prev_time;
	unsigned int prev_frizz_ms;
	unsigned int calc_sec, calc_msec;
	float sampling_time_sec;
	char buff[128];
	const char *char_dev_name = env->GetStringUTFChars(file_name, 0);
	strcpy(buff, char_dev_name);

	fp = fopen(buff, "w");

	//jclass jcls  = env->GetObjectClass(thiz);
	//jmethodID sensorDataCallback = env->GetMethodID(jcls, "sensorDataCallback", "(J[F[F[FF)V");
	//jmethodID sensorDataCallback = env->GetMethodID(jcls, "sensorDataCallback", "(JJJ[F[F[FF)V");
	prev_time.tv_sec  = 0;
	prev_time.tv_usec = 0;

	java_gyro = env->NewFloatArray(sizeof(f_value));
	java_accl = env->NewFloatArray(sizeof(f_value));
	java_mag  = env->NewFloatArray(sizeof(f_value));

	gyro.code  = SENSOR_TYPE_GYROSCOPE_UNCALIBRATED;
	accl.code  = SENSOR_TYPE_ACCELEROMETER;
	mag.code   = SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
	press.code = SENSOR_TYPE_PRESSURE;

	start_flag = 0;

	for(;;) {

		ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_DATA, &gyro);

		if(((prev_time.tv_sec != gyro.time.tv_sec) || (prev_time.tv_usec != gyro.time.tv_usec)) &&
		   ((prev_time.tv_sec != 0) && (prev_time.tv_usec != 0))) {

			ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_DATA, &accl);
			ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_DATA, &mag);
			ioctl(fd, FRIZZ_IOCTL_SENSOR_GET_DATA, &press);

			float *gyro_value = (float *)gyro.f32_value;
			float *accl_value = (float *)accl.f32_value;
			float *mag_value  = (float *)mag.f32_value;
/*
			env->SetFloatArrayRegion(java_gyro, 0, 6, gyro_value);
			env->SetFloatArrayRegion(java_accl, 0, 6, accl_value);
			env->SetFloatArrayRegion(java_mag,  0, 6, mag_value);*/

			convert_float(press.f32_value[0], &java_press);

			if(gyro.frizz_ms < 1000) {
				calc_sec  = 0;
				calc_msec = gyro.frizz_ms;
				sampling_time_sec = (float)(calc_msec - prev_frizz_ms) / (float)1000.0;
			} else {
				calc_sec  = gyro.frizz_ms / 1000;
				calc_msec = gyro.frizz_ms - (calc_sec * 1000);
				sampling_time_sec = (float)(gyro.frizz_ms - prev_frizz_ms) / (float)1000.0;
			}
			/*
			if(prev_time.tv_usec < gyro.time.tv_usec) {
				//sampling_time_sec = (float)(gyro.time.tv_usec - prev_time.tv_usec) / (float)1000000.0;
			} else {
				//sampling_time_sec = (float)((1000000 + gyro.time.tv_usec) - prev_time.tv_usec) / (float)1000000.0;
			}*/

			fprintf(fp, "%ld %ld %.9f %.9f %.9f %.9f %.9f %.9f %.9f %.9f %.9f na na na na %.9f %.9f %ld\n",
					calc_sec, calc_msec,
					gyro_value[0], gyro_value[1], gyro_value[2],
					accl_value[0], accl_value[1], accl_value[2],
					mag_value[0],  mag_value[1],  mag_value[2],
					java_press, sampling_time_sec, (long)gyro.frizz_ms);

		}

		prev_time.tv_sec  = gyro.time.tv_sec;
		prev_time.tv_usec = gyro.time.tv_usec;
		prev_frizz_ms     = gyro.frizz_ms;

		usleep(sleep_usec);

		if(start_flag == 1) {
			fclose(fp);
			break;
		}

	}

	return 0;
}

}

