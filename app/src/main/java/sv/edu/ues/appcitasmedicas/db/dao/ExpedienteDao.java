package sv.edu.ues.appcitasmedicas.db.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
import sv.edu.ues.appcitasmedicas.db.entity.ExpedienteEntity;
@Dao
public interface ExpedienteDao {
    @Insert long insert(ExpedienteEntity e);
    @Update void update(ExpedienteEntity e);
    @Delete void delete(ExpedienteEntity e);
    @Query("SELECT * FROM expedientes ORDER BY fecha_apertura DESC")
    LiveData<List<ExpedienteEntity>> getAll();
    @Query("SELECT * FROM expedientes WHERE paciente_id=:pacienteId LIMIT 1")
    LiveData<ExpedienteEntity> getByPaciente(int pacienteId);
}
