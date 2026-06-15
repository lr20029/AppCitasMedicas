package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.DialogExpedienteBinding;
import sv.edu.ues.appcitasmedicas.databinding.FragmentExpedientesBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.ExpedienteEntity;
import sv.edu.ues.appcitasmedicas.db.entity.PacienteEntity;
import sv.edu.ues.appcitasmedicas.ui.adapters.ExpedientesAdapter;

public class ExpedientesFragment extends Fragment implements ExpedientesAdapter.OnExpedienteClickListener {
    private FragmentExpedientesBinding binding;
    private ExpedientesAdapter adapter;
    private AppDatabase db;
    private List<ExpedienteEntity> allExpedientes = new ArrayList<>();
    private List<PacienteEntity> pacientes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExpedientesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        requireActivity().setTitle("Expedientes Médicos");
        adapter = new ExpedientesAdapter(new ArrayList<>(), this);
        binding.rvExpedientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpedientes.setAdapter(adapter);
        binding.fabAddExpediente.setOnClickListener(v->showExpedienteDialog(null));
        db.expedienteDao().getAll().observe(getViewLifecycleOwner(), list->{
            allExpedientes=list!=null?list:new ArrayList<>();
            adapter.updateList(allExpedientes);
            binding.tvEmpty.setVisibility(allExpedientes.isEmpty()?View.VISIBLE:View.GONE);
        });
        db.pacienteDao().getAll().observe(getViewLifecycleOwner(), list->
                pacientes=list!=null?list:new ArrayList<>());
    }

    private void showExpedienteDialog(@Nullable ExpedienteEntity expediente){
        DialogExpedienteBinding d=DialogExpedienteBinding.inflate(getLayoutInflater());
        boolean isEdit=expediente!=null;
        if(isEdit){
            d.etNumeroExp.setText(expediente.numeroExpediente);
            d.etAntFamiliares.setText(expediente.antecedentesFamiliares);
            d.etAntPersonales.setText(expediente.antecedentesPersonales);
            d.etMedicamentos.setText(expediente.medicamentosActuales);
            d.etEnfCronicas.setText(expediente.enfermedadesCronicas);
            d.etCirugias.setText(expediente.cirugiasPrevias);
            d.etPeso.setText(String.valueOf(expediente.peso));
            d.etTalla.setText(String.valueOf(expediente.talla));
        } else {
            d.etNumeroExp.setText("EXP-"+new SimpleDateFormat("yyyyMMddHHmm",Locale.getDefault()).format(new Date()));
        }
        List<String> nombres=new ArrayList<>(); nombres.add("Seleccionar paciente...");
        int idx=0;
        for(int i=0;i<pacientes.size();i++){
            PacienteEntity p=pacientes.get(i); nombres.add(p.nombre+" "+p.apellido);
            if(isEdit&&p.id==expediente.pacienteId) idx=i+1;
        }
        ArrayAdapter<String> spa=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_item,nombres);
        spa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        d.spPacienteExp.setAdapter(spa);
        if(isEdit) d.spPacienteExp.setSelection(idx);

        new AlertDialog.Builder(requireContext(),R.style.MediCareDialog)
                .setTitle(isEdit?"Editar Expediente":"Nuevo Expediente").setView(d.getRoot())
                .setPositiveButton(getString(R.string.save),(dl,w)->{
                    int pIdx=d.spPacienteExp.getSelectedItemPosition();
                    if(pIdx==0){Toast.makeText(requireContext(),"Selecciona un paciente",Toast.LENGTH_SHORT).show();return;}
                    PacienteEntity ps=pacientes.get(pIdx-1);
                    ExpedienteEntity e=isEdit?expediente:new ExpedienteEntity();
                    e.pacienteId=ps.id;
                    e.numeroExpediente=d.etNumeroExp.getText().toString().trim();
                    e.antecedentesFamiliares=d.etAntFamiliares.getText().toString().trim();
                    e.antecedentesPersonales=d.etAntPersonales.getText().toString().trim();
                    e.medicamentosActuales=d.etMedicamentos.getText().toString().trim();
                    e.enfermedadesCronicas=d.etEnfCronicas.getText().toString().trim();
                    e.cirugiasPrevias=d.etCirugias.getText().toString().trim();
                    try{e.peso=Float.parseFloat(d.etPeso.getText().toString().trim());
                        e.talla=Float.parseFloat(d.etTalla.getText().toString().trim());}
                    catch(NumberFormatException ex){e.peso=0;e.talla=0;}
                    if(!isEdit) e.fechaApertura=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());
                    Executors.newSingleThreadExecutor().execute(()->{
                        if(isEdit) db.expedienteDao().update(e); else db.expedienteDao().insert(e);
                        requireActivity().runOnUiThread(()->Toast.makeText(requireContext(),
                                getString(R.string.success_save),Toast.LENGTH_SHORT).show());
                    });
                }).setNegativeButton(getString(R.string.cancel),null).show();
    }

    @Override public void onEditExpediente(ExpedienteEntity e){showExpedienteDialog(e);}
    @Override public void onDeleteExpediente(ExpedienteEntity e){
        new AlertDialog.Builder(requireContext(),R.style.MediCareDialog)
                .setTitle("Eliminar").setMessage(getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.confirm),(d,w)->
                        Executors.newSingleThreadExecutor().execute(()->db.expedienteDao().delete(e)))
                .setNegativeButton(getString(R.string.cancel),null).show();
    }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
