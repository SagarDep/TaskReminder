package ie.cit.brian.taskreminder;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ie.cit.brian.taskreminder.activities.LocationActivity;
import ie.cit.brian.taskreminder.activities.LoginActivity;
import ie.cit.brian.taskreminder.activities.MainActivity;
import ie.cit.brian.taskreminder.activities.MapsActivity;
import ie.cit.brian.taskreminder.activities.PreferenceActivity;
import ie.cit.brian.taskreminder.activities.TaskActivity;

/**
 * Created by briancoveney on 12/16/15.
 */
public final class UtilityClass extends TaskActivity {


    // Effective Java Item 4: "Enforce non-insatiability with a private constructor"
    private UtilityClass(){
        //Not Called
    }



    /** Writing and Reading to Internal Storage  **/
    public static String readFromFile(Context context)
    {
        String result = "";

        try {
            InputStream inputStream = context.openFileInput("myFile");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = null;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                result = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static void writeToFile(Context context, String data)
    {
        try {
            FileOutputStream outputStreamWriter
                    = context.getApplicationContext().openFileOutput("myFile", context.MODE_PRIVATE);
            outputStreamWriter.write(data.getBytes());
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** launch Activities  **/
    public static void launchMainActivity(Context context){
        Intent main = new Intent(context, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(main);
    }

    public static void launchGoogleMapsActivity(Context context){
        Intent map = new Intent(context, MapsActivity.class);
        map.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(map);
    }

    public static void launchLocationActivity(Context context){
        Intent loc = new Intent(context, LocationActivity.class);
        loc.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loc);
    }

    public static void launchSettingsActivity(Context context){
        Intent pref = new Intent(context, PreferenceActivity.class);
        pref.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(pref);
    }

    public static void launchLoginActivity(Context context){
        Intent login = new Intent(context, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(login);
    }


    /** Notifications **/



}
