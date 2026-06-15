package sv.edu.ues.appcitasmedicas.ui.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import sv.edu.ues.appcitasmedicas.R;
import sv.edu.ues.appcitasmedicas.db.entity.ExpedienteEntity;

public class ExpedientesAdapter extends RecyclerView.Adapter<ExpedientesAdapter.ExpedienteViewHolder> {
    private List<ExpedienteEntity> expedientes;
    private final OnExpedienteClickListener listener;

    public interface OnExpedienteClickListener {
        void onEditExpediente(ExpedienteEntity e);
        void onDeleteExpediente(ExpedienteEntity e);
    }

    public ExpedientesAdapter(List<ExpedienteEntity> expedientes, OnExpedienteClickListener listener){
        this.expedientes=expedientes; this.listener=listener;
    }

    public void updateList(List<ExpedienteEntity> l){this.expedientes=l;notifyDataSetChanged();}

    @NonNull @Override
    public ExpedienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        return new ExpedienteViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expediente,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpedienteViewHolder h,int position){
        ExpedienteEntity e=expedientes.get(position);
        h.tvNumero.setText(e.numeroExpediente);
        h.tvFecha.setText("Abierto: "+e.fechaApertura);
        h.tvImc.setText(e.peso>0&&e.talla>0?
                String.format("IMC: %.1f",e.peso/(e.talla*e.talla)):"IMC: N/D");
        h.tvEnfCronicas.setText(e.enfermedadesCronicas!=null&&!e.enfermedadesCronicas.isEmpty()?
                e.enfermedadesCronicas:"Sin enfermedades crónicas");
        h.btnEdit.setOnClickListener(v->listener.onEditExpediente(e));
        h.btnDelete.setOnClickListener(v->listener.onDeleteExpediente(e));
    }

    @Override public int getItemCount(){return expedientes.size();}

    static class ExpedienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero,tvFecha,tvImc,tvEnfCronicas;
        View btnEdit,btnDelete;
        ExpedienteViewHolder(@NonNull View v){
            super(v);
            tvNumero=v.findViewById(R.id.tvNumeroExpediente);
            tvFecha=v.findViewById(R.id.tvFechaApertura);
            tvImc=v.findViewById(R.id.tvImc);
            tvEnfCronicas=v.findViewById(R.id.tvEnfCronicas);
            btnEdit=v.findViewById(R.id.btnEditExpediente);
            btnDelete=v.findViewById(R.id.btnDeleteExpediente);
        }
    }
}