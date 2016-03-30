package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Brendan Otten on 3/30/2016.
 */
public class getEventIntentService extends IntentService {

    Process sh;

    public getEventIntentService(){
        super("getEventIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            File eventsFile = new File("sdcard/events.txt");
            if(eventsFile.exists()){
                eventsFile.delete();
            }
            eventsFile.createNewFile();
            sh = Runtime.getRuntime().exec(new String[]{"su","-c","getevent -lt > sdcard/events.txt"});
            OutputStream os = new FileOutputStream(eventsFile);
            String s;
            BufferedReader br = new BufferedReader(new FileReader(eventsFile));
            int numEvents = -1;

            while(Globals.recording){

            }

            while(br.ready()){
                s = br.readLine();
                if(s.contains("DOWN")){
                    if(numEvents == -1){
                        numEvents++;
                    }
                    else {
                        numEvents++;

                        s = br.readLine();
                        s = br.readLine();
                        //register x value
                        if(s.length() == 93) {
                            String x = s.substring(s.length() - 20, s.length() - 12);

                            s = br.readLine();
                            if(s.length() == 93) {
                                //register y value
                                String y = s.substring(s.length() - 20, s.length() - 12);


                                //translate hex to int coordinates
                                int x_cor = Integer.parseInt(x, 16);
                                int y_cor = Integer.parseInt(y, 16);
                                Log.v("getEvent", "X: " + x_cor);
                                Log.v("getEvent", "Y: " + y_cor);
                                //Add event coordinates to bugreport
                                BugReport.getInstance().addGetEvent(x_cor, y_cor);
                            }
                        }


                    }
                }

                //Log.v("getEvent", s);
            }
            //Log.v("getEvent", "Number of events: " + numEvents);
            br.close();
            sh.destroy();
            Log.v("DataCollection", "Process Killed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
