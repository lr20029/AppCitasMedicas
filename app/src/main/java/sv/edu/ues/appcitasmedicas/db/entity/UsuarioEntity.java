package sv.edu.ues.appcitasmedicas.db.entity;

import androidx.room.*;

@Entity(tableName = "usuarios")
public class UsuarioEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nombre;
    public String email;
    public String password;
    public String especialidad;
    public String fotoUri;
    public String fechaRegistro;

    public UsuarioEntity() {}

    @Ignore
    public UsuarioEntity(String nombre, String email, String password,
                         String especialidad, String fechaRegistro) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.especialidad = especialidad;
        this.fechaRegistro = fechaRegistro;
    }
}