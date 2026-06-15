package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        DialogPacienteBinding d=DialogPacienteBinding.inflate(getLayoutInflater());
        boolean isEdit=paciente!=null;
        if(isEdit){
            d.etNombre.setText(paciente.nombre); d.etApellido.setText(paciente.apellido);
            d.etDui.setText(paciente.dui); d.etFechaNac.setText(paciente.fechaNacimiento);
            d.etTelefono.setText(paciente.telefono); d.etEmail.setText(paciente.email);
            d.etDireccion.setText(paciente.direccion); d.etTipoSangre.setText(paciente.tipoSangre);
            d.etAlergias.setText(paciente.alergias);
        }
        new AlertDialog.Builder(requireContext(),R.style.MediCareDialog)
                .setTitle(isEdit?"Editar Paciente":"Nuevo Paciente").setView(d.getRoot())
                .setPositiveButton(getString(R.string.save),(dl,w)->{
                    String nombre=d.etNombre.getText().toString().trim();
                    String apellido=d.etApellido.getText().toString().trim();
                    if(nombre.isEmpty()||apellido.isEmpty()){
                        Toast.makeText(requireContext(),getString(R.string.error_empty),Toast.LENGTH_SHORT).show();return;}
                    PacienteEntity p=isEdit?paciente:new PacienteEntity();
                    p.nombre=nombre; p.apellido=apellido;
                    p.dui=d.etDui.getText().toString().trim();
                    p.fechaNacimiento=d.etFechaNac.getText().toString().trim();
                    p.telefono=d.etTelefono.getText().toString().trim();
                    p.email=d.etEmail.getText().toString().trim();
                    p.direccion=d.etDireccion.getText().toString().trim();
                    p.tipoSangre=d.etTipoSangre.getText().toString().trim();
                    p.alergias=d.etAlergias.getText().toString().trim();
                    if(!isEdit) p.fechaRegistro=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());
                    Executors.newSingleThreadExecutor().execute(()->{
                        if(isEdit) db.pacienteDao().update(p); else db.pacienteDao().insert(p);
                        requireActivity().runOnUiThread(()->Toast.makeText(requireContext(),
                                getString(R.string.success_save),Toast.LENGTH_SHORT).show());
                    });
                }).setNegativeButton(getString(R.string.cancel),null).show();
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
