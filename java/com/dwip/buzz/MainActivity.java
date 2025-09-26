package com.dwip.buzz;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends Activity {
    
    // UI components
    private LinearLayout startScreen;
    private RelativeLayout gameScreen;
    private Button playButton;
    private TextView scoreText;
    private View tapArea;
    
    // Game variables
    private Vibrator vibrator;
    private Random random;
    private int score = 0;
    private boolean isGameRunning = false;
    private boolean waitingForTap = false;
    
    // Vibration patterns
    private final long[] pattern1 = {0, 200};
    private final long[] pattern2 = {0, 100, 200};
    private final long[] pattern3 = {0, 100};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);
        
        // Initialize views
        startScreen = (LinearLayout) findViewById(R.id.startScreen);
        gameScreen = (RelativeLayout) findViewById(R.id.gameScreen);
        playButton = (Button) findViewById(R.id.playButton);
        scoreText = (TextView) findViewById(R.id.scoreText);
        tapArea = findViewById(R.id.tapArea);
        
        // Get vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        random = new Random();
        
        // Set up button click
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startGame();
            }
        });
        
        // Set up tap area - make sure it doesn't cover the score
        tapArea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handleTap();
            }
        });
        
        // Make sure score is visible by bringing it to front
        scoreText.bringToFront();
    }
    
    private void startGame() {
        // Switch screens
        startScreen.setVisibility(View.GONE);
        gameScreen.setVisibility(View.VISIBLE);
        
        // Reset score and make sure it's visible
        score = 0;
        scoreText.setText("Score: " + score);
        scoreText.setVisibility(View.VISIBLE);
        scoreText.bringToFront();
        
        // Start game
        isGameRunning = true;
        waitingForTap = false;
        
        // Quick start - 2 seconds to first vibration
        tapArea.postDelayed(new Runnable() {
            public void run() {
                if (isGameRunning) {
                    triggerVibration();
                }
            }
        }, 2000);
    }
    
    private void triggerVibration() {
        if (!isGameRunning) return;
        
        // Choose random vibration count (1-3)
        int count = random.nextInt(3) + 1;
        waitingForTap = true;
        
        // Select pattern based on count
        long[] pattern;
        if (count == 1) {
            pattern = pattern1;
        } else if (count == 2) {
            pattern = pattern2;
        } else {
            pattern = pattern3;
        }
        
        // Vibrate
        if (vibrator != null) {
            vibrator.vibrate(pattern, -1);
        }
        
        // Give player 1.5 seconds to tap after vibration starts
        tapArea.postDelayed(new Runnable() {
            public void run() {
                if (waitingForTap) {
                    // Player missed - didn't tap within 1.5 seconds
                    score--;
                    updateScore();
                    waitingForTap = false;
                    
                    // Quick next vibration after miss
                    if (isGameRunning) {
                        tapArea.postDelayed(new Runnable() {
                            public void run() {
                                if (isGameRunning) {
                                    triggerVibration();
                                }
                            }
                        }, 1000);
                    }
                }
            }
        }, 1500);
    }
    
    private void handleTap() {
        if (!isGameRunning) return;
        
        if (waitingForTap) {
            // Correct tap within the time window
            score++;
            waitingForTap = false;
            updateScore();
            
            // Cancel the miss check
            tapArea.removeCallbacks(null);
            
            // Quick next vibration after successful tap
            if (isGameRunning) {
                tapArea.postDelayed(new Runnable() {
                    public void run() {
                        if (isGameRunning) {
                            triggerVibration();
                        }
                    }
                }, 800);
            }
        } else {
            // Wrong tap - no vibration expected
            if (score > 0) {
                score--;
                updateScore();
            }
        }
    }
    
    private void updateScore() {
        runOnUiThread(new Runnable() {
            public void run() {
                scoreText.setText("Score: " + score);
                scoreText.bringToFront(); // Keep score on top
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isGameRunning) {
            endGame();
        }
    }
    
    private void endGame() {
        isGameRunning = false;
        waitingForTap = false;
        
        if (vibrator != null) {
            vibrator.cancel();
        }
        
        tapArea.removeCallbacks(null);
        
        // Return to start screen
        runOnUiThread(new Runnable() {
            public void run() {
                gameScreen.setVisibility(View.GONE);
                startScreen.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (isGameRunning) {
            endGame();
        } else {
            super.onBackPressed();
        }
    }
}
