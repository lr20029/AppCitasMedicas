package sv.edu.ues.appcitasmedicas.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS="AppCitasMedicasPrefs";
    private final SharedPreferences prefs;

    public PrefsManager(Context ctx){prefs=ctx.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}

    public void saveSession(int id,String nombre,String email,String especialidad){
        prefs.edit().putInt("user_id",id).putString("user_name",nombre)
                .putString("user_email",email).putString("user_esp",especialidad)
                .putBoolean("logged_in",true).apply();
    }

    public void clearSession(){
        prefs.edit().remove("user_id").remove("user_name").remove("user_email")
                .remove("user_esp").putBoolean("logged_in",false).apply();
    }

    public boolean isLoggedIn(){return prefs.getBoolean("logged_in",false);}
    public int getUserId(){return prefs.getInt("user_id",-1);}
    public String getUserName(){return prefs.getString("user_name","");}
    public String getUserEmail(){return prefs.getString("user_email","");}
    public String getUserEspecialidad(){return prefs.getString("user_esp","");}

    public void setDarkMode(boolean enabled){
        prefs.edit().putBoolean("dark_mode",enabled).apply();
    }

    public boolean isDarkMode(){return prefs.getBoolean("dark_mode",false);}

    public void setRememberEmail(boolean remember,String email){
        prefs.edit().putBoolean("remember_email",remember)
                .putString("saved_email",remember?email:"").apply();
    }

    public boolean isRememberEmail(){return prefs.getBoolean("remember_email",false);}
    public String getSavedEmail(){return prefs.getString("saved_email","");}
}
