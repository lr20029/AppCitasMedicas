package sv.edu.ues.appcitasmedicas.db.entity;

import androidx.room.*;

@Entity(tableName = "diagnosticos",
        foreignKeys = @ForeignKey(
                entity = ExpedienteEntity.class,
                parentColumns = "id",
                childColumns = "expediente_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("expediente_id")})
public class DiagnosticoEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "expediente_id")
    public int expedienteId;

    public String descripcion;
    public String tratamiento;
    public String fecha;
    public String medico;
    public String observaciones;
}