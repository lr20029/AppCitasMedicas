package sv.edu.ues.appcitasmedicas.db.entity;

import androidx.room.*;

@Entity(tableName = "citas",
        foreignKeys = @ForeignKey(
                entity = PacienteEntity.class,
                parentColumns = "id",
                childColumns = "paciente_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("paciente_id")})
public class CitaEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "paciente_id")
    public int pacienteId;

    @ColumnInfo(name = "paciente_nombre")
    public String pacienteNombre;

    public String fecha;
    public String hora;
    public String motivo;
    public String estado;
    public String notas;
    public String tipoConsulta;
}