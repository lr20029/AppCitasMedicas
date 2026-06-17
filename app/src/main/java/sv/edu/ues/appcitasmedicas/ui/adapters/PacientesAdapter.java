package sv.edu.ues.appcitasmedicas.ui.adapters;

import android.net.Uri;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.util.List;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.db.entity.PacienteEntity;

public class PacientesAdapter extends RecyclerView.Adapter<PacientesAdapter.PacienteViewHolder> {
    private List<PacienteEntity> pacientes;
    private final OnPacienteClickListener listener;

    public interface OnPacienteClickListener {
        void onEditPaciente(PacienteEntity p);
        void onDeletePaciente(PacienteEntity p);
    }

    public PacientesAdapter(List<PacienteEntity> pacientes, OnPacienteClickListener listener) {
        this.pacientes = pacientes; this.listener = listener;
    }

    public void updateList(List<PacienteEntity> l){this.pacientes = l; notifyDataSetChanged();}

    @NonNull @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new PacienteViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder h, int position){
        PacienteEntity p = pacientes.get(position);
        h.tvNombre.setText(p.nombre + " " + p.apellido);
        h.tvDui.setText(p.dui != null && !p.dui.isEmpty() ? p.dui : "Sin DUI");
        h.tvTelefono.setText(p.telefono != null && !p.telefono.isEmpty() ? p.telefono : "Sin teléfono");
        h.tvTipoSangre.setText(p.tipoSangre != null && !p.tipoSangre.isEmpty() ? p.tipoSangre : "-");

        if (p.fotoUri != null && !p.fotoUri.isEmpty()) {
            File imgFile = new File(p.fotoUri);
            if (imgFile.exists()) {
                h.ivFoto.setImageURI(Uri.fromFile(imgFile));
                h.ivFoto.setVisibility(View.VISIBLE);
                h.tvInitial.setVisibility(View.GONE);
            } else {
                setDefaultAvatar(h, p);
            }
        } else {
            setDefaultAvatar(h, p);
        }

        h.btnEdit.setOnClickListener(v -> listener.onEditPaciente(p));
        h.btnDelete.setOnClickListener(v -> listener.onDeletePaciente(p));
    }

    private void setDefaultAvatar(PacienteViewHolder h, PacienteEntity p) {
        h.tvInitial.setText(p.nombre.isEmpty() ? "?" : String.valueOf(p.nombre.charAt(0)).toUpperCase());
        h.tvInitial.setVisibility(View.VISIBLE);
        h.ivFoto.setVisibility(View.GONE);
    }

    @Override public int getItemCount(){return pacientes.size();}

    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDui, tvTelefono, tvTipoSangre, tvInitial;
        CircleImageView ivFoto;
        View btnEdit, btnDelete;

        PacienteViewHolder(@NonNull View v){
            super(v);
            tvNombre = v.findViewById(R.id.tvNombrePaciente);
            tvDui = v.findViewById(R.id.tvDui);
            tvTelefono = v.findViewById(R.id.tvTelefono);
            tvTipoSangre = v.findViewById(R.id.tvTipoSangre);
            tvInitial = v.findViewById(R.id.tvInitial);
            ivFoto = v.findViewById(R.id.ivRowFotoPaciente);
            btnEdit = v.findViewById(R.id.btnEditPaciente);
            btnDelete = v.findViewById(R.id.btnDeletePaciente);
        }
    }
}
