package sv.edu.ues.appcitasmedicas.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.ActivityMainBinding;
import sv.edu.ues.appcitasmedicas.ui.auth.LoginActivity;
import sv.edu.ues.appcitasmedicas.ui.fragments.*;
import sv.edu.ues.appcitasmedicas.util.PrefsManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public ActivityMainBinding binding;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PrefsManager(this);
        int modo = prefs.isDarkMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != modo) {
            AppCompatDelegate.setDefaultNightMode(modo);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.nav_dashboard, R.string.nav_dashboard);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);

        View headerView = binding.navView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.tvDrawerNombre);
        TextView tvEmail = headerView.findViewById(R.id.tvDrawerEmail);
        TextView tvEsp = headerView.findViewById(R.id.tvDrawerEspecialidad);
        if (tvNombre != null) tvNombre.setText(prefs.getUserName());
        if (tvEmail != null) tvEmail.setText(prefs.getUserEmail());
        if (tvEsp != null) tvEsp.setText(prefs.getUserEspecialidad());

        com.google.android.material.switchmaterial.SwitchMaterial switchDark =
                binding.getRoot().findViewById(R.id.switchDarkModeDrawer);
        if (switchDark != null) {
            switchDark.setChecked(prefs.isDarkMode());
            switchDark.setOnCheckedChangeListener((b, isChecked) -> {
                prefs.setDarkMode(isChecked);
                AppCompatDelegate.setDefaultNightMode(isChecked ?
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) loadFragment(new DashboardFragment());
            else if (id == R.id.nav_citas) loadFragment(new CitasFragment());
            else if (id == R.id.nav_pacientes) loadFragment(new PacientesFragment());
            else if (id == R.id.nav_expedientes) loadFragment(new ExpedientesFragment());
            else if (id == R.id.nav_perfil) loadFragment(new PerfilFragment());
            return true;
        });

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) loadFragment(new DashboardFragment());
        else if (id == R.id.nav_citas) loadFragment(new CitasFragment());
        else if (id == R.id.nav_pacientes) loadFragment(new PacientesFragment());
        else if (id == R.id.nav_expedientes) loadFragment(new ExpedientesFragment());
        else if (id == R.id.nav_logout) showLogoutDialog();
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.MediCareDialog)
                .setTitle("Cerrar sesión").setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.confirm), (d, w) -> {
                    prefs.clearSession();
                    startActivity(new Intent(this, LoginActivity.class)); finish();
                }).setNegativeButton(getString(R.string.cancel), null).show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }
}