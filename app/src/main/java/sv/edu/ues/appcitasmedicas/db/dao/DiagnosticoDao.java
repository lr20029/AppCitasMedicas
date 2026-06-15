package sv.edu.ues.appcitasmedicas.db.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
import sv.edu.ues.appcitasmedicas.db.entity.DiagnosticoEntity;
@Dao
public interface DiagnosticoDao {
    @Insert long insert(DiagnosticoEntity d);
    @Update void update(DiagnosticoEntity d);
    @Delete void delete(DiagnosticoEntity d);
    @Query("SELECT * FROM diagnosticos WHERE expediente_id=:expedienteId ORDER BY fecha DESC")
    LiveData<List<DiagnosticoEntity>> getByExpediente(int expedienteId);
}