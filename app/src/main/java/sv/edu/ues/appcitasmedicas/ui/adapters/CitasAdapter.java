package sv.edu.ues.appcitasmedicas.ui.adapters;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.db.entity.CitaEntity;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {
    private List<CitaEntity> citas;
    private final OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onEditCita(CitaEntity cita);
        void onDeleteCita(CitaEntity cita);
    }

    public CitasAdapter(List<CitaEntity> citas, OnCitaClickListener listener) {
        this.citas=citas; this.listener=listener;
    }

    public void updateList(List<CitaEntity> newList) { this.citas=newList; notifyDataSetChanged(); }

    @NonNull @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CitaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder h, int position) {
        CitaEntity c=citas.get(position);
        h.tvPaciente.setText(c.pacienteNombre);
        h.tvFechaHora.setText(c.fecha + "  •  " + c.hora);
        h.tvMotivo.setText(c.motivo);
        h.tvEstado.setText(c.estado);
        switch(c.estado){
            case "CONFIRMADA": h.tvEstado.setBackgroundResource(R.drawable.bg_status_confirmed);
                h.tvEstado.setTextColor(Color.parseColor("#2E7D32")); break;
            case "CANCELADA": h.tvEstado.setBackgroundResource(R.drawable.bg_status_cancelled);
                h.tvEstado.setTextColor(Color.parseColor("#C62828")); break;
            case "COMPLETADA": h.tvEstado.setBackgroundColor(Color.parseColor("#E3F2FD"));
                h.tvEstado.setTextColor(Color.parseColor("#1565C0")); break;
            default: h.tvEstado.setBackgroundResource(R.drawable.bg_status_pending);
                h.tvEstado.setTextColor(Color.parseColor("#E65100"));
        }
        h.btnEdit.setOnClickListener(v->listener.onEditCita(c));
        h.btnDelete.setOnClickListener(v->listener.onDeleteCita(c));
    }

    @Override public int getItemCount(){return citas.size();}

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaciente,tvFechaHora,tvMotivo,tvEstado;
        View btnEdit,btnDelete;
        CitaViewHolder(@NonNull View v){
            super(v);
            tvPaciente=v.findViewById(R.id.tvPaciente);
            tvFechaHora=v.findViewById(R.id.tvFechaHora);
            tvMotivo=v.findViewById(R.id.tvMotivo);
            tvEstado=v.findViewById(R.id.tvEstado);
            btnEdit=v.findViewById(R.id.btnEditCita);
            btnDelete=v.findViewById(R.id.btnDeleteCita);
        }
    }
}