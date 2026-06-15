package sv.edu.ues.appcitasmedicas.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.ActivityLoginBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.UsuarioEntity;
import sv.edu.ues.appcitasmedicas.ui.main.MainActivity;
import sv.edu.ues.appcitasmedicas.util.PrefsManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDatabase db;
    private PrefsManager prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        prefs = new PrefsManager(this);

        if (prefs.isRememberEmail()) {
            binding.etEmail.setText(prefs.getSavedEmail());
            binding.cbRemember.setChecked(true);
        }

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // Boton salir de la app
        binding.btnSalir.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        executor.execute(() -> {
            UsuarioEntity user = db.usuarioDao().login(email, password);
            runOnUiThread(() -> {
                binding.progressLogin.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                if (user != null) {
                    prefs.saveSession(user.id, user.nombre,
                            user.email, user.especialidad);
                    prefs.setRememberEmail(
                            binding.cbRemember.isChecked(), email);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    binding.tilEmail.setError("Credenciales incorrectas");
                    binding.tilPassword.setError(" ");
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}