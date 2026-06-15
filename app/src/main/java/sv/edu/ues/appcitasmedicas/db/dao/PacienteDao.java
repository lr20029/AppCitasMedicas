package sv.edu.ues.appcitasmedicas.db.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
import sv.edu.ues.appcitasmedicas.db.entity.PacienteEntity;
@Dao
public interface PacienteDao {
    @Insert long insert(PacienteEntity p);
    @Update void update(PacienteEntity p);
    @Delete void delete(PacienteEntity p);
    @Query("SELECT * FROM pacientes ORDER BY nombre ASC")
    LiveData<List<PacienteEntity>> getAll();
    @Query("SELECT * FROM pacientes WHERE id=:id LIMIT 1")
    LiveData<PacienteEntity> getById(int id);
    @Query("SELECT COUNT(*) FROM pacientes")
    int count();
}