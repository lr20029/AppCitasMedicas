package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    private ActivityResultLauncher<String> pickImageLauncher;
    private String selectedPhotoUriString = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String localSavedPath = copyUriToInternalStorage(uri);
                        if (localSavedPath != null) {
                            selectedPhotoUriString = localSavedPath;
                            binding.ivAvatarPerfil.setPadding(0, 0, 0, 0);
                            binding.ivAvatarPerfil.setImageURI(Uri.parse(localSavedPath));
                        }
                    }
                }
        );
    }

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
        binding.etEditNombre.setText(prefs.getUserName());
        binding.etEditEspecialidad.setText(prefs.getUserEspecialidad());
        cargarFotoUsuario();
        binding.ivAvatarPerfil.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
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

    private void cargarFotoUsuario() {
        Executors.newSingleThreadExecutor().execute(() -> {
            UsuarioEntity user = db.usuarioDao().findByEmail(prefs.getUserEmail());
            if (user != null && user.fotoUri != null && !user.fotoUri.isEmpty()) {
                File file = new File(user.fotoUri);
                if (file.exists()) {
                    requireActivity().runOnUiThread(() -> {
                        selectedPhotoUriString = user.fotoUri;
                        binding.ivAvatarPerfil.setPadding(0, 0, 0, 0);
                        binding.ivAvatarPerfil.setImageURI(Uri.fromFile(file));
                    });
                }
            }
        });
    }

    private void updateProfile(){
        String nombre = binding.etEditNombre.getText().toString().trim();
        String especialidad = binding.etEditEspecialidad.getText().toString().trim();

        if(nombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(()->{
            UsuarioEntity user = db.usuarioDao().findByEmail(prefs.getUserEmail());
            if(user != null){
                user.nombre = nombre;
                user.especialidad = especialidad;
                user.fotoUri = selectedPhotoUriString; // Persistir la ruta asignada en la DB

                db.usuarioDao().update(user);

                // Actualizamos también la sesión local en las preferencias
                prefs.saveSession(user.id, user.nombre, user.email, user.especialidad);

                requireActivity().runOnUiThread(()->{
                    binding.tvNombrePerfil.setText(user.nombre);
                    binding.tvEspecialidadPerfil.setText(user.especialidad);
                    Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "user_profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}