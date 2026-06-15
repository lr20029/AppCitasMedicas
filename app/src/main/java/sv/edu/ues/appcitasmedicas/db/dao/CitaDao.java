package sv.edu.ues.appcitasmedicas.db.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
import sv.edu.ues.appcitasmedicas.db.entity.CitaEntity;
@Dao
public interface CitaDao {
    @Insert long insert(CitaEntity c);
    @Update void update(CitaEntity c);
    @Delete void delete(CitaEntity c);
    @Query("SELECT * FROM citas ORDER BY fecha DESC,hora ASC")
    LiveData<List<CitaEntity>> getAll();
    @Query("SELECT * FROM citas WHERE paciente_id=:pacienteId ORDER BY fecha DESC")
    LiveData<List<CitaEntity>> getByPaciente(int pacienteId);
    @Query("SELECT COUNT(*) FROM citas WHERE estado='PENDIENTE'")
    int countPendientes();
    @Query("SELECT COUNT(*) FROM citas WHERE fecha=:today")
    int countHoy(String today);
}
