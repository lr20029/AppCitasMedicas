package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.FragmentPerfilBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.UsuarioEntity;
import sv.edu.ues.appcitasmedicas.ui.auth.LoginActivity;
import sv.edu.ues.appcitasmedicas.util.PrefsManager;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private PrefsManager prefs;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new PrefsManager(requireContext());
        db = AppDatabase.getInstance(requireContext());
        requireActivity().setTitle("Perfil");

        binding.tvNombrePerfil.setText(prefs.getUserName());
        binding.tvEmailPerfil.setText(prefs.getUserEmail());
        binding.tvEspecialidadPerfil.setText(prefs.getUserEspecialidad());

        binding.switchDarkMode.setOnCheckedChangeListener(null);
        binding.switchDarkMode.setChecked(prefs.isDarkMode());
        binding.switchDarkMode.setOnCheckedChangeListener((b, isChecked) -> {
            prefs.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnActualizarPerfil.setOnClickListener(v -> updateProfile());
        binding.btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext(), R.style.MediCareDialog)
                        .setTitle("Cerrar sesión").setMessage(getString(R.string.logout_confirm))
                        .setPositiveButton(getString(R.string.confirm),(d,w)->{
                            prefs.clearSession();
                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        }).setNegativeButton(getString(R.string.cancel),null).show());
    }

    private void updateProfile(){
        String nombre=binding.etEditNombre.getText().toString().trim();
        String especialidad=binding.etEditEspecialidad.getText().toString().trim();
        if(nombre.isEmpty()) return;
        Executors.newSingleThreadExecutor().execute(()->{
            UsuarioEntity user=db.usuarioDao().findByEmail(prefs.getUserEmail());
            if(user!=null){
                if(!nombre.isEmpty()) user.nombre=nombre;
                if(!especialidad.isEmpty()) user.especialidad=especialidad;
                db.usuarioDao().update(user);
                prefs.saveSession(user.id,user.nombre,user.email,user.especialidad);
                requireActivity().runOnUiThread(()->{
                    binding.tvNombrePerfil.setText(user.nombre);
                    binding.tvEspecialidadPerfil.setText(user.especialidad);
                    Toast.makeText(requireContext(),"Perfil actualizado",Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}