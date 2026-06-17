package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.DialogPacienteBinding;
import sv.edu.ues.appcitasmedicas.databinding.FragmentPacientesBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.PacienteEntity;
import sv.edu.ues.appcitasmedicas.ui.adapters.PacientesAdapter;

public class PacientesFragment extends Fragment implements PacientesAdapter.OnPacienteClickListener {
    private FragmentPacientesBinding binding;
    private PacientesAdapter adapter;
    private AppDatabase db;
    private List<PacienteEntity> allPacientes = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;
    private DialogPacienteBinding currentDialogBinding;
    private String selectedPhotoUriString = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentDialogBinding != null) {
                        String savedLocalPath = copyUriToInternalStorage(uri);
                        if (savedLocalPath != null) {
                            selectedPhotoUriString = savedLocalPath;
                            currentDialogBinding.ivFotoPaciente.setPadding(0,0,0,0);
                            currentDialogBinding.ivFotoPaciente.setImageURI(Uri.parse(savedLocalPath));
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPacientesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        requireActivity().setTitle("Pacientes");
        adapter = new PacientesAdapter(new ArrayList<>(), this);
        binding.rvPacientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPacientes.setAdapter(adapter);
        binding.etSearch.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){filterPacientes(s.toString());}
            @Override public void afterTextChanged(Editable s){}
        });
        binding.fabAddPaciente.setOnClickListener(v->showPacienteDialog(null));
        db.pacienteDao().getAll().observe(getViewLifecycleOwner(), list->{
            allPacientes=list!=null?list:new ArrayList<>();
            adapter.updateList(allPacientes);
            binding.tvEmpty.setVisibility(allPacientes.isEmpty()?View.VISIBLE:View.GONE);
        });
    }

    private void filterPacientes(String q){
        if(q.isEmpty()){adapter.updateList(allPacientes);return;}
        List<PacienteEntity> f=new ArrayList<>();
        for(PacienteEntity p:allPacientes)
            if((p.nombre+" "+p.apellido).toLowerCase().contains(q.toLowerCase())||
                    (p.dui!=null&&p.dui.contains(q))) f.add(p);
        adapter.updateList(f);
    }

    private void showPacienteDialog(@Nullable PacienteEntity paciente){
        DialogPacienteBinding d = DialogPacienteBinding.inflate(getLayoutInflater());
        currentDialogBinding = d;
        boolean isEdit = paciente != null;

        String hoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        if(isEdit){
            d.etNombre.setText(paciente.nombre); d.etApellido.setText(paciente.apellido);
            d.etDui.setText(paciente.dui); d.etFechaNac.setText(paciente.fechaNacimiento);
            d.etTelefono.setText(paciente.telefono); d.etEmail.setText(paciente.email);
            d.etDireccion.setText(paciente.direccion); d.etTipoSangre.setText(paciente.tipoSangre);
            d.etAlergias.setText(paciente.alergias);

            selectedPhotoUriString = paciente.fotoUri;
            if (paciente.fotoUri != null && !paciente.fotoUri.isEmpty()) {
                d.ivFotoPaciente.setPadding(0,0,0,0);
                d.ivFotoPaciente.setImageURI(Uri.parse(paciente.fotoUri));
            }
        } else {
            d.etFechaNac.setText(hoy);
            selectedPhotoUriString = null;
        }

        d.ivFotoPaciente.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        String[] tiposSangre = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        ArrayAdapter<String> tsAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, tiposSangre);
        d.etTipoSangre.setAdapter(tsAdapter);

        d.etFechaNac.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(d.etFechaNac.getText().toString());
                if (fecha != null) cal.setTime(fecha);
            } catch (Exception ignored) {}
            new android.app.DatePickerDialog(requireContext(),
                    (view, year, month, day) -> {
                        String fecha = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", day, month + 1, year);
                        d.etFechaNac.setText(fecha);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        d.etDui.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String str = s.toString().replace("-", "");
                StringBuilder formatted = new StringBuilder();
                if (str.length() > 0) {
                    if (str.length() <= 8) {
                        formatted.append(str);
                    } else {
                        formatted.append(str.substring(0, 8)).append("-").append(str.substring(8));
                    }
                }
                d.etDui.setText(formatted.toString());
                d.etDui.setSelection(formatted.length());
                isUpdating = false;
            }
        });

        new AlertDialog.Builder(requireContext(),R.style.MediCareDialog)
                .setTitle(isEdit?"Editar Paciente":"Nuevo Paciente").setView(d.getRoot())
                .setPositiveButton(getString(R.string.save),(dl,w)->{
                    String nombre=d.etNombre.getText().toString().trim();
                    String apellido=d.etApellido.getText().toString().trim();
                    String dui = d.etDui.getText().toString().trim();

                    if(nombre.isEmpty()||apellido.isEmpty()){
                        Toast.makeText(requireContext(),getString(R.string.error_empty),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!dui.isEmpty() && dui.length() != 10) {
                        Toast.makeText(requireContext(), "Formato de DUI incorrecto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PacienteEntity p=isEdit?paciente:new PacienteEntity();
                    p.nombre=nombre; p.apellido=apellido;
                    p.dui=dui;
                    p.fechaNacimiento=d.etFechaNac.getText().toString().trim();
                    p.telefono=d.etTelefono.getText().toString().trim();
                    p.email=d.etEmail.getText().toString().trim();
                    p.direccion=d.etDireccion.getText().toString().trim();
                    p.tipoSangre=d.etTipoSangre.getText().toString().trim();
                    p.alergias=d.etAlergias.getText().toString().trim();
                    p.fotoUri = selectedPhotoUriString; // Assigning the image file string path

                    if(!isEdit) p.fechaRegistro=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());

                    Executors.newSingleThreadExecutor().execute(()->{
                        if(isEdit) db.pacienteDao().update(p); else db.pacienteDao().insert(p);
                        requireActivity().runOnUiThread(()->Toast.makeText(requireContext(),
                                getString(R.string.success_save),Toast.LENGTH_SHORT).show());
                    });
                }).setNegativeButton(getString(R.string.cancel),null)
                .setOnDismissListener(dialog -> currentDialogBinding = null) // Avoid leaks
                .show();
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
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

    @Override public void onEditPaciente(PacienteEntity p){showPacienteDialog(p);}
    @Override public void onDeletePaciente(PacienteEntity p){
        new AlertDialog.Builder(requireContext(),R.style.MediCareDialog)
                .setTitle("Eliminar").setMessage(getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.confirm),(d,w)->
                        Executors.newSingleThreadExecutor().execute(()->db.pacienteDao().delete(p)))
                .setNegativeButton(getString(R.string.cancel),null).show();
    }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}