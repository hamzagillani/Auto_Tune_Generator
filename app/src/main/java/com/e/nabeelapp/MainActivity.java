package com.e.nabeelapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    private EditText name,number;
    private Button save;
    private TextView show_name,show_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        osc = new Oscillator();

        // View assignments and initial setup
        textHz = (EditText) findViewById(R.id.text_hz);
        textVol = (EditText) findViewById(R.id.text_vol);

        buttonToggle = (ToggleButton) findViewById(R.id.toggle_play);

        seekFreq = (SeekBar) findViewById(R.id.slider_freq);
        // seekFreq starts at Oscillator.MIN_FREQUENCY, so subtract that from
        // its max value
        seekFreq.setMax(Oscillator.MAX_FREQUENCY - Oscillator.MIN_FREQUENCY);
        // Sets the initial seekFreq progress
        seekFreq.setProgress(osc.getFreq() - Oscillator.MIN_FREQUENCY);

        seekVol = (SeekBar) findViewById(R.id.slider_vol);
        seekVol.setMax(100);
        seekVol.setProgress(50);

        spinWave = (Spinner) findViewById(R.id.spinner_wave);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.wave_choices,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinWave.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Updates the frequency once the textHz EditText loses focus
        textHz.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int freqValue = Integer.parseInt(((EditText) v).getText().toString());
                    if (freqValue > Oscillator.MAX_FREQUENCY) {
                        osc.setFreq(Oscillator.MAX_FREQUENCY);
                    }
                    else {
                        osc.setFreq(freqValue);
                    }

                    // Since the SeekBar starts at Oscillator.MIN_FREQUENCY, it
                    // needs to be subtracted
                    // from the value (i.e. 20Hz gets set at 0 on seekFreq)
                    seekFreq.setProgress(osc.getFreq() - Oscillator.MIN_FREQUENCY);
                }
            }
        });

        // Updates the volume from textVol once 'enter' has been pressed on the
        // soft keyboard.
        textVol.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Prevents a value greater than 100 from being sent to the
                    // volume
                    String value = ((EditText) v).getText().toString();
                    int volValue = Integer.parseInt(value);
                    if (volValue > 100) {
                        osc.setVolume(100);
                    }
                    else {
                        osc.setVolume(volValue);
                    }
                    seekVol.setProgress(osc.getVolume());
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        // Handles the pausing and playing of the oscillator from the
        // ToggleButton
        buttonToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    osc.play();
                    new Thread(new Runnable() {
                        public void run() {
                            // Continuously fills the buffer while the
                            // oscillator is playing
                            while (osc.getIsPlaying()) {
                                osc.fillBuffer();
                            }
                        }
                    }).start();
                }
                else {
                    osc.pause();
                }
            }
        });

        // Updates the oscillator's frequency based on the progress of seekFreq
        seekFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                osc.setFreq(Oscillator.MIN_FREQUENCY + seekBar.getProgress());
                textHz.setText("" + osc.getFreq());
            }
        });

        // Updates the oscillator's volume based on the progress of seekVol
        seekVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                osc.setVolume(seekBar.getProgress());
                textVol.setText("" + osc.getVolume());
            }
        });

        // Updates the oscillator's waveform based on the selection in spinWave
        spinWave.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                osc.setWave(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Stops the oscillator playback and releases the AudioTrack resources
    @Override
    protected void onPause() {
        super.onPause();
        osc.stop();
        buttonToggle.setChecked(false);
    }

    private EditText textHz, textVol;
    private Oscillator osc;
    private ToggleButton buttonToggle;
    private SeekBar seekFreq, seekVol;
    private Spinner spinWave;
    }

