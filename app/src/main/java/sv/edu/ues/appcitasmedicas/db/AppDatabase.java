package sv.edu.ues.appcitasmedicas.db;

import android.content.Context;
import androidx.room.*;
import sv.edu.ues.appcitasmedicas.db.dao.*;
import sv.edu.ues.appcitasmedicas.db.entity.*;

@Database(entities = {
        UsuarioEntity.class, PacienteEntity.class, CitaEntity.class,
        ExpedienteEntity.class, DiagnosticoEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract UsuarioDao usuarioDao();
    public abstract PacienteDao pacienteDao();
    public abstract CitaDao citaDao();
    public abstract ExpedienteDao expedienteDao();
    public abstract DiagnosticoDao diagnosticoDao();

    public static AppDatabase getInstance(Context context){
        if(INSTANCE==null){
            synchronized(AppDatabase.class){
                if(INSTANCE==null){
                    INSTANCE=Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class,"appcitasmedicas_db")
                            .fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}