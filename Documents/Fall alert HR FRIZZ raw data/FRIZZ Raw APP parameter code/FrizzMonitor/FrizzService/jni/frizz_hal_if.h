#include <linux/time.h>
#include <linux/ioctl.h>

#define IOC_MAGIC '9'

#define FRIZZ_IOCTL_SENSOR   1   /*!< Start Index of Android IOCTL */
#define FRIZZ_IOCTL_MCC      32  /*!< Start Index of MCC IOCTL */
#define FRIZZ_IOCTL_HARDWARE 64  /*!< Start Index of Hardware IOCTL */

#define FRIZZ_MAX_PACKET_SIZE 66
#define FRIZZ_MAX_SENSOR_DATA_SIZE	(62) ///< Max data size of sensor event


typedef struct {
	uint32_t sensor_id;
	uint32_t test_loop;
} sensor_info;

/*!@enum Sensor type
 * @breaf Android Sensor type and MCC Sensor type
 */
typedef enum {

	SENSOR_TYPE_FIFO_EMPTY = 0,

	//These const are Android Senosr Type. //HAL
	SENSOR_TYPE_ACCELEROMETER = 1,
	SENSOR_TYPE_MAGNETIC_FIELD,// 2
	SENSOR_TYPE_ORIENTATION,// 3
	SENSOR_TYPE_GYROSCOPE,  // 4
	SENSOR_TYPE_LIGHT,		// 5
	SENSOR_TYPE_PRESSURE,   //6
	SENSOR_TYPE_TEMPERATURE,//7
	SENSOR_TYPE_PROXIMITY,   //8
	SENSOR_TYPE_GRAVITY,     //9
	SENSOR_TYPE_LINEAR_ACCELERATION,//10
	SENSOR_TYPE_ROTATION_VECTOR,   // 11
	SENSOR_TYPE_RELATIVE_HUMIDITY, // 12
	SENSOR_TYPE_AMBIENT_TEMPERATURE, // 13
	SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED, //14
	SENSOR_TYPE_GAME_ROTATION_VECTOR,        // 15
	SENSOR_TYPE_GYROSCOPE_UNCALIBRATED,      // 16
	SENSOR_TYPE_SIGNIFICANT_MOTION,          // 17
	SENSOR_TYPE_STEP_DETECTOR,              // 18
	SENSOR_TYPE_STEP_COUNTER,               // 19
	SENSOR_TYPE_GEOMAGNETIC_ROTATION_VECTOR,// 20
	SENSOR_TYPE_HEART_RATE, //21
	SENSOR_TYPE_TILT_DETECTOR, //22

	//This const is MCC Sensor Type. (Extended Senosr)	NDK
	SENSOR_TYPE_MAGNET_RAW,// 23
	SENSOR_TYPE_GYRO_RAW, // 24
	SENSOR_TYPE_ACCEL_POWER, // 25
	SENSOR_TYPE_ACCEL_LPF,   // 26
	SENSOR_TYPE_ACCEL_LINEAR, // 27
	SENSOR_TYPE_MAGNET_PARAMETER,// 28
	SENSOR_TYPE_MAGNET_CALIB_SOFT,// 29
	SENSOR_TYPE_MAGNET_LPF,// 30
	SENSOR_TYPE_GYRO_LPF, // 31
	SENSOR_TYPE_DIRECTION,// 32
	SENSOR_TYPE_POSTURE, // 33
	SENSOR_TYPE_ROTATION_MATRIX,// 34
	SENSOR_TYPE_PDR,// 35
	SENSOR_TYPE_VELOCITY,// 36
	SENSOR_TYPE_RELATIVE_POSITION, // 37
	SENSOR_TYPE_CYCLIC_TIMER,// 38
	SENSOR_TYPE_DEBUG_QUEUE_IN,// 39
	SENSOR_TYPE_ISP,// 40
	SENSOR_TYPE_ACCEL_FALL_DOWN,// 41
	SENSOR_TYPE_ACCEL_POS_DET,// 42
	SENSOR_TYPE_PDR_GEOFENCING,// 43
	SENSOR_TYPE_GESTURE,// 44
	SENSOR_TYPE_MIGRATION_LENGTH,//45
	SENSOR_TYPE_MAGNET_CALIB_RAW, //46
	SENSOR_TYPE_BLOOD_PRESSURE, //47
	SENSOR_TYPE_PPG_RAW,// 48
	SENSOR_TYPE_WEARING_DETECTOR,// 49
	SENSOR_TYPE_MOTION_DETECTOR, // 50
	SENSOR_TYPE_BLOOD_PRESSURE_LEARN,	// 51
	
	SENSOR_TYPE_STAIR_DETECTOR,			// 52
	SENSOR_TYPE_ACTIVITY_DETECTOR,		// 53
	SENSOR_TYPE_MOTION_SENSING,			// 54
	
	SENSOR_TYPE_CALORIE,		// 55
	SENSOR_TYPE_BIKE_DETECTOR, // 56

} libsensors_type_e;

/*! @struct sensor_enable_t
 *  @brief
 */
typedef struct {
	int code; /*!< Android Sensor type*/
	int flag; /*!< 0 : disable sensor, 1:enable sensor*/
} sensor_enable_t;

/*! @struct sensor_delay_t
 *  @brief  control sensor delay time
 */
typedef struct {
	int code; /*!< Android Sensor type*/
	int ms;   /*!< millisecond*/
} sensor_delay_t;

typedef struct {
	int code;
	int number;
} sensor_version_t;

/*! @struct sensor_data_t
 *  @brief input sensor data
 */
typedef struct {
	int            code;
	struct timeval time;
	unsigned int frizz_ms;

	union {
		//float    f32_value[6];
	unsigned int    f32_value[FRIZZ_MAX_SENSOR_DATA_SIZE];
		uint64_t u64_value;
		/*
		struct {
		    unsigned int rpos[2];
		    unsigned int velo[2];
		    unsigned int total_dst;
		    unsigned int count;
		};*/
	};
} sensor_data_t;

/*! @struct batch_t
 *  @brief set batch mode
 */
typedef struct {
	int fifo_full_flag;
	int code;
	uint64_t period_ns;
	uint64_t timeout_ns;
	uint64_t simulate_timeout_ns;
} batch_t;

/*!< set enable or disable.*/
#define FRIZZ_IOCTL_SENSOR_SET_ENABLE       _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR,     sensor_enable_t*)

/*!< get sensor status. (enable or disable)*/
#define FRIZZ_IOCTL_SENSOR_GET_ENABLE       _IOR(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 1, sensor_enable_t*)

/*!< set sensor delay time */
#define FRIZZ_IOCTL_SENSOR_SET_DELAY        _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 2, sensor_delay_t*)

/*!< get sensor delay time */
#define FRIZZ_IOCTL_SENSOR_GET_DELAY        _IOR(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 3, sensor_delay_t*)

/*!< get sensor data*/
#define FRIZZ_IOCTL_SENSOR_GET_DATA         _IOR(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 4, sensor_data_t*)

/*!< return time  that can be stored software fifo.*/
#define FRIZZ_IOCTL_SENSOR_SIMULATE_TIMEOUT _IOR(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 5, batch_t*)

/*!< set batch mode */
#define FRIZZ_IOCTL_SENSOR_SET_BATCH        _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 6, batch_t*)

/*!< flush software fifo in frizz*/
#define FRIZZ_IOCTL_SENSOR_FLUSH_FIFO       _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 7, void*)

#define FRIZZ_IOCTL_SENSOR_SET_PDR_HOLDING  _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 8, int*)

#define FRIZZ_IOCTL_SENSOR_SET_PDR_HOLDING  _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 8, int*)

#define FRIZZ_IOCTL_SENSOR_OFFSET_PDR_POSITION _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 9, unsigned int*)

#define FRIZZ_IOCTL_SENSOR_OFFSET_PDR_DIRECTION _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 10, unsigned int*)

#define FRIZZ_IOCTL_SENSOR_GET_VERSION      _IOR(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 11, sensor_version_t*)

#define FRIZZ_IOCTL_SENSOR_SET_FRIZZ_COMMAND    _IOW(IOC_MAGIC, FRIZZ_IOCTL_SENSOR + 12, unsigned int*)

/*!< set enable or disable.*/
#define FRIZZ_IOCTL_MCC_SET_ENABLE       _IOW(IOC_MAGIC, FRIZZ_IOCTL_MCC,     sensor_enable_t*)

/*!< get sensor status. (enable or disable)*/
#define FRIZZ_IOCTL_MCC_GET_ENABLE       _IOR(IOC_MAGIC, FRIZZ_IOCTL_MCC + 1, sensor_enable_t*)

/*!< get sensor data*/
#define FRIZZ_IOCTL_MCC_GET_DATA         _IOR(IOC_MAGIC, FRIZZ_IOCTL_MCC + 2, sensor_data_t*)

/*!< set sensor delay time */
#define FRIZZ_IOCTL_MCC_SET_DELAY        _IOW(IOC_MAGIC, FRIZZ_IOCTL_MCC + 3, sensor_delay_t*)

/*!< get sensor delay time */
#define FRIZZ_IOCTL_MCC_GET_DELAY        _IOR(IOC_MAGIC, FRIZZ_IOCTL_MCC + 4, sensor_delay_t*)

/*!< reset frizz*/
#define FRIZZ_IOCTL_HARDWARE_RESET              _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE,     void*)

/*!< stall frizz*/
#define FRIZZ_IOCTL_HARDWARE_STALL              _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE + 1, void*)

/*!< download firmware*/
#define FRIZZ_IOCTL_HARDWARE_DOWNLOAD_FIRMWARE  _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE + 2, char*)

/*!< enable frizz gpio*/
#define FRIZZ_IOCTL_HARDWARE_ENABLE_GPIO        _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE + 3, void*)
#define FRIZZ_IOCTL_FW_TEST                     _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE + 4, sensor_info*)
#define FRIZZ_IOCTL_I2C_TEST                     _IOW(IOC_MAGIC, FRIZZ_IOCTL_HARDWARE + 5, void*)
