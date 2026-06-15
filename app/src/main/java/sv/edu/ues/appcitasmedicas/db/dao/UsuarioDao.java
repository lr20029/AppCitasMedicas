package sv.edu.ues.appcitasmedicas.db.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
import sv.edu.ues.appcitasmedicas.db.entity.UsuarioEntity;
@Dao
public interface UsuarioDao {
    @Insert long insert(UsuarioEntity u);
    @Update void update(UsuarioEntity u);
    @Delete void delete(UsuarioEntity u);
    @Query("SELECT * FROM usuarios WHERE email=:email AND password=:password LIMIT 1")
    UsuarioEntity login(String email,String password);
    @Query("SELECT * FROM usuarios WHERE email=:email LIMIT 1")
    UsuarioEntity findByEmail(String email);
    @Query("SELECT * FROM usuarios WHERE id=:id LIMIT 1")
    LiveData<UsuarioEntity> getById(int id);
}
