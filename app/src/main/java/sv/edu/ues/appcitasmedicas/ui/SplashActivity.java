package sv.edu.ues.appcitasmedicas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.ui.auth.LoginActivity;
import sv.edu.ues.appcitasmedicas.ui.main.MainActivity;
import sv.edu.ues.appcitasmedicas.util.PrefsManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PrefsManager prefs = new PrefsManager(this);
        if (prefs.isDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> dest = prefs.isLoggedIn() ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(this, dest));
            finish();
        }, 2000);
    }
}