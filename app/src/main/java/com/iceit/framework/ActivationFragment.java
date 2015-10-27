package com.iceit.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.iceit.R;

public class ActivationFragment extends Fragment implements SensorEventListener {

    private static final String TAG = ActivationFragment.class.getSimpleName();
    private static final int COUNTDOWN_LENGTH = 10000;

    private String storedFullName;
    private String storedContactsName;
    private String storedContactsNumber;

    private TextView mTextView;
    private Button btnActivate;
    private Button btnCancel;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 2000;
    private SoundPool _sp;
    private int _soundId;

    private CountDownTimer countdown;

    //TODO: loading of this Fragment needs to be fixed
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_activation, container, false);

        btnActivate = (Button) rootView.findViewById(R.id.btnActivate);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
//        btnCancel.setVisibility(View.INVISIBLE);
        mTextView = (TextView) rootView.findViewById(R.id.countdown_txt);

        getDBData();
        createBeepSample();

		// TODO: check if all data exists, if none exists don't allow activate button
        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAccelorometer();
//                btnActivate.setVisibility(View.INVISIBLE);
//                btnCancel.setVisibility(View.VISIBLE);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopCountdown();
//                btnActivate.setVisibility(View.VISIBLE);
//                btnCancel.setVisibility(View.INVISIBLE);
            }
        });

		return rootView;
	}

    private Boolean getDBData() {
        SharedPreferences fullNameFile = getActivity().getSharedPreferences(ProfileFragment.PROFILE_FILE, 0);
        storedFullName = fullNameFile.getString("fullName", "");

        SharedPreferences contactsFile = getActivity().getSharedPreferences(ContactsFragment.CONTACTS_FILE, 0);
        storedContactsName = contactsFile.getString("contactName", "");
        storedContactsNumber = contactsFile.getString("contactNumber", "");
        return true;
    }

    private void startAccelorometer() {
        // start Accelorometer
        createSensorListener(getActivity().getApplicationContext());
        registerListener();
    }

    private void stopAccelorometer() {
        // stop Accelorometer
        unRegisterListener();
    }

    private void startCountdown() {
        // turn volume on phone up to max for this purpose
        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        countdown =  new CountDownTimer(COUNTDOWN_LENGTH, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextView.setText(String.valueOf((millisUntilFinished / 1000)));
                _sp.play(_soundId, 1, 1, 0, 0, 1);
            }

            public void onFinish() {

                mTextView.setText(R.string.smsSent);
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

                sendDistressSignal(0);
            }

            // TODO: on cancel stopAccelerator and start it again
        };
        countdown.start();
    }

    private void stopCountdown() {
        countdown.cancel();
    }

    // send a distress call
    // either SMS or voice messge
    private void sendDistressSignal(int type) {
        if(type == 0) {
            sendSMS(storedContactsNumber, "ICE IT has detected " + storedFullName + " is in an emergency, please assist them immediately.");
        }
    }

    private void sendSMS(String phoneNumber, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void createSensorListener(Context context) {
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerListener() {
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegisterListener() {
        senSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d(TAG, "onSensorChanged");
//                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                    startCountdown();
                    stopAccelorometer();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored
    }

    private void createBeepSample() {
        // turn volume on phone up to max for this purpose
        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        _sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

        /** soundId for Later handling of sound pool **/
        _soundId = _sp.load(getActivity().getBaseContext(), R.raw.beep, 1); // in 2nd param u have to pass your sound

        MediaPlayer mPlayer = MediaPlayer.create(getActivity().getBaseContext(), R.raw.beep); // in 2nd param u have to pass your sound
        //mPlayer.prepare();
        mPlayer.start();
    }
}
