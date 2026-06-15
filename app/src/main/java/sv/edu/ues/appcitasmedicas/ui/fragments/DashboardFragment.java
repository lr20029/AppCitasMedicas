package sv.edu.ues.appcitasmedicas.ui.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.databinding.FragmentDashboardBinding;
import sv.edu.ues.appcitasmedicas.db.AppDatabase;
import sv.edu.ues.appcitasmedicas.ui.main.MainActivity;
import sv.edu.ues.appcitasmedicas.util.PrefsManager;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AppDatabase db;
    private PrefsManager prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        prefs = new PrefsManager(requireContext());
        requireActivity().setTitle("Panel Principal");

        String hora = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        int h = Integer.parseInt(hora);
        String saludo = h < 12 ? "Buenos dias" : h < 18 ? "Buenas tardes" : "Buenas noches";
        binding.tvSaludo.setText(saludo + ", Dr(a). " + prefs.getUserName());
        binding.tvFechaHoy.setText(
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        loadStats();

        binding.cardCitas.setOnClickListener(v ->
                navigate(new CitasFragment(), R.id.nav_citas));
        binding.cardPacientes.setOnClickListener(v ->
                navigate(new PacientesFragment(), R.id.nav_pacientes));
        binding.cardExpedientes.setOnClickListener(v ->
                navigate(new ExpedientesFragment(), R.id.nav_expedientes));
        binding.cardPerfil.setOnClickListener(v ->
                navigate(new PerfilFragment(), R.id.nav_perfil));
    }

    private void loadStats() {
        String hoy = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            int citasHoy = db.citaDao().countHoy(hoy);
            int pendientes = db.citaDao().countPendientes();
            int pacientes = db.pacienteDao().count();

            if (!isAdded() || binding == null) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded() || binding == null) return;
                binding.tvCitasHoy.setText(String.valueOf(citasHoy));
                binding.tvPendientes.setText(String.valueOf(pendientes));
                binding.tvTotalPacientes.setText(String.valueOf(pacientes));
            });
        });
    }

    private void navigate(Fragment f, int menuId) {
        if (!isAdded()) return;
        ((MainActivity) requireActivity()).loadFragment(f);
        ((MainActivity) requireActivity()).binding.bottomNav.setSelectedItemId(menuId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}