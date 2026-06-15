package sv.edu.ues.appcitasmedicas.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.ActivityRegisterBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.UsuarioEntity;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = AppDatabase.getInstance(this);
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> { startActivity(new Intent(this, LoginActivity.class)); finish(); });
    }

    private void attemptRegister() {
        String nombre = binding.etNombre.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();
        String especialidad = binding.etEspecialidad.getText().toString().trim();
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty), Toast.LENGTH_SHORT).show(); return;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden"); return;
        }
        binding.progressRegister.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);
        executor.execute(() -> {
            UsuarioEntity existing = db.usuarioDao().findByEmail(email);
            runOnUiThread(() -> {
                if (existing != null) {
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    binding.tilEmailReg.setError(getString(R.string.error_email_exists)); return;
                }
                String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                UsuarioEntity u = new UsuarioEntity(nombre, email, password, especialidad, fecha);
                executor.execute(() -> {
                    db.usuarioDao().insert(u);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class)); finish();
                    });
                });
            });
        });
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
