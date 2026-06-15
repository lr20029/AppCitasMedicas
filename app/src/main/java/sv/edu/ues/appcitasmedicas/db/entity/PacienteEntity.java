package sv.edu.ues.appcitasmedicas.db.entity;
import androidx.room.*;
@Entity(tableName="pacientes")
public class PacienteEntity {
    @PrimaryKey(autoGenerate=true) public int id;
    public String nombre,apellido,dui,fechaNacimiento,telefono,email,direccion,tipoSangre,alergias,fotoUri,fechaRegistro;
}