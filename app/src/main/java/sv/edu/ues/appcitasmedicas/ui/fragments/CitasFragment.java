package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.os.Bundle;
import android.text.*;
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
import sv.edu.ues.appcitasmedicas.databinding.DialogCitaBinding;
import sv.edu.ues.appcitasmedicas.databinding.FragmentCitasBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.db.entity.CitaEntity;
import sv.edu.ues.appcitasmedicas.db.entity.PacienteEntity;
import sv.edu.ues.appcitasmedicas.ui.adapters.CitasAdapter;

public class CitasFragment extends Fragment implements CitasAdapter.OnCitaClickListener {

    private FragmentCitasBinding binding;
    private CitasAdapter adapter;
    private AppDatabase db;
    private List<CitaEntity> allCitas = new ArrayList<>();
    private List<PacienteEntity> pacientes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCitasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        requireActivity().setTitle("Citas Medicas");

        adapter = new CitasAdapter(new ArrayList<>(), this);
        binding.rvCitas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCitas.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterCitas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.fabAddCita.setOnClickListener(v -> showCitaDialog(null));

        db.citaDao().getAll().observe(getViewLifecycleOwner(), list -> {
            allCitas = list != null ? list : new ArrayList<>();
            adapter.updateList(allCitas);
            binding.tvEmpty.setVisibility(allCitas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        db.pacienteDao().getAll().observe(getViewLifecycleOwner(), list ->
                pacientes = list != null ? list : new ArrayList<>());
    }

    private void filterCitas(String q) {
        if (q.isEmpty()) { adapter.updateList(allCitas); return; }
        List<CitaEntity> f = new ArrayList<>();
        for (CitaEntity c : allCitas)
            if (c.pacienteNombre.toLowerCase().contains(q.toLowerCase())
                    || c.fecha.contains(q)
                    || c.estado.toLowerCase().contains(q.toLowerCase()))
                f.add(c);
        adapter.updateList(f);
    }

    private void showCitaDialog(@Nullable CitaEntity cita) {
        DialogCitaBinding d = DialogCitaBinding.inflate(getLayoutInflater());
        boolean isEdit = cita != null;

        final String[] fechaSeleccionada = {""};
        final String[] horaSeleccionada = {""};

        if (isEdit) {
            d.etMotivo.setText(cita.motivo);
            d.tvFecha.setText(cita.fecha);
            d.tvHora.setText(cita.hora);
            d.etNotas.setText(cita.notas);
            fechaSeleccionada[0] = cita.fecha;
            horaSeleccionada[0] = cita.hora;
        } else {
            String hoy = new SimpleDateFormat("dd/MM/yyyy",
                    Locale.getDefault()).format(new Date());
            d.tvFecha.setText(hoy);
            fechaSeleccionada[0] = hoy;
        }

        // DatePicker
        d.btnPickFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(),
                    (view2, year, month, day) -> {
                        String fecha = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", day, month + 1, year);
                        d.tvFecha.setText(fecha);
                        fechaSeleccionada[0] = fecha;
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // TimePicker
        d.btnPickHora.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new android.app.TimePickerDialog(requireContext(),
                    (view2, hour, minute) -> {
                        String hora = String.format(Locale.getDefault(),
                                "%02d:%02d", hour, minute);
                        d.tvHora.setText(hora);
                        horaSeleccionada[0] = hora;
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
            ).show();
        });

        // AutoComplete paciente
        List<String> nombres = new ArrayList<>();
        int selIdx = -1;
        for (int i = 0; i < pacientes.size(); i++) {
            PacienteEntity p = pacientes.get(i);
            nombres.add(p.nombre + " " + p.apellido);
            if (isEdit && p.id == cita.pacienteId) selIdx = i;
        }
        ArrayAdapter<String> spa = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombres);
        d.spPaciente.setAdapter(spa);
        if (isEdit && selIdx >= 0) d.spPaciente.setText(nombres.get(selIdx), false);

        // AutoComplete estado
        String[] estados = {"PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA"};
        ArrayAdapter<String> ea = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, estados);
        d.spEstado.setAdapter(ea);
        if (isEdit) d.spEstado.setText(cita.estado, false);

        new AlertDialog.Builder(requireContext(), R.style.MediCareDialog)
                .setTitle(isEdit ? "Editar Cita" : "Nueva Cita")
                .setView(d.getRoot())
                .setPositiveButton(getString(R.string.save), (dl, w) -> {
                    String motivo = d.etMotivo.getText().toString().trim();
                    String pacNombre = d.spPaciente.getText().toString().trim();
                    String estado = d.spEstado.getText().toString().trim();
                    int pIdx = -1;
                    for (int i = 0; i < pacientes.size(); i++) {
                        String n = pacientes.get(i).nombre + " " + pacientes.get(i).apellido;
                        if (n.equals(pacNombre)) { pIdx = i; break; }
                    }

                    if (motivo.isEmpty() || fechaSeleccionada[0].isEmpty()
                            || horaSeleccionada[0].isEmpty() || pIdx < 0 || estado.isEmpty()) {
                        Toast.makeText(requireContext(),
                                getString(R.string.error_empty),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PacienteEntity ps = pacientes.get(pIdx);
                    CitaEntity c = isEdit ? cita : new CitaEntity();
                    c.pacienteId = ps.id;
                    c.pacienteNombre = ps.nombre + " " + ps.apellido;
                    c.motivo = motivo;
                    c.fecha = fechaSeleccionada[0];
                    c.hora = horaSeleccionada[0];
                    c.notas = d.etNotas.getText().toString().trim();
                    c.estado = estado;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (isEdit) db.citaDao().update(c);
                        else db.citaDao().insert(c);
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        getString(R.string.success_save),
                                        Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onEditCita(CitaEntity c) {

        // Si está COMPLETADA mostrar aviso pero dejar ver
        if ("COMPLETADA".equals(c.estado)) {
            new AlertDialog.Builder(requireContext(), R.style.MediCareDialog)
                    .setTitle("Cita completada")
                    .setMessage("Esta cita ya fue completada.\n\n" +
                            "Puedes editarla si necesitas corregir algun dato.")
                    .setPositiveButton("Editar de todas formas",
                            (d, w) -> showCitaDialog(c))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return;
        }

        showCitaDialog(c);
    }

    @Override
    public void onDeleteCita(CitaEntity c) {

        // Verificar si la cita está CONFIRMADA (en proceso)
        if ("CONFIRMADA".equals(c.estado)) {
            new AlertDialog.Builder(requireContext(), R.style.MediCareDialog)
                    .setTitle("No se puede eliminar")
                    .setMessage("La cita de " + c.pacienteNombre +
                            " esta CONFIRMADA y en proceso.\n\n" +
                            "Para eliminarla primero cambia el estado a " +
                            "CANCELADA o COMPLETADA.")
                    .setPositiveButton("Entendido", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        // Si no está confirmada, preguntar confirmación normal
        new AlertDialog.Builder(requireContext(), R.style.MediCareDialog)
                .setTitle("Eliminar Cita")
                .setMessage("Paciente: " + c.pacienteNombre +
                        "\nFecha: " + c.fecha + " - " + c.hora +
                        "\n\n" + getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.confirm), (d, w) ->
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.citaDao().delete(c);
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "Cita eliminada correctamente",
                                            Toast.LENGTH_SHORT).show());
                        }))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}