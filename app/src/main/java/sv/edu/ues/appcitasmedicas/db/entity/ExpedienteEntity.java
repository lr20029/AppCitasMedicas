package sv.edu.ues.appcitasmedicas.db.entity;

import androidx.room.*;

@Entity(tableName = "expedientes",
        foreignKeys = @ForeignKey(
                entity = PacienteEntity.class,
                parentColumns = "id",
                childColumns = "paciente_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("paciente_id")})
public class ExpedienteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "paciente_id")
    public int pacienteId;

    @ColumnInfo(name = "numero_expediente")
    public String numeroExpediente;

    @ColumnInfo(name = "antecedentes_familiares")
    public String antecedentesFamiliares;

    @ColumnInfo(name = "antecedentes_personales")
    public String antecedentesPersonales;

    @ColumnInfo(name = "medicamentos_actuales")
    public String medicamentosActuales;

    @ColumnInfo(name = "enfermedades_cronicas")
    public String enfermedadesCronicas;

    @ColumnInfo(name = "cirugias_previas")
    public String cirugiasPrevias;

    public float peso;
    public float talla;

    @ColumnInfo(name = "fecha_apertura")
    public String fechaApertura;
}