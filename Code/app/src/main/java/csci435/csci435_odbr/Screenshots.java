package csci435.csci435_odbr;

/**
 * Created by Brendan Otten on 3/27/2016.
 */
public class Screenshots {

    // Lol container class #struct

    String filename;
    long timestamp;

    public Screenshots(String file, long time){
        filename = file;
        timestamp = time;
    }

    public void add_timestamp(long time){
        timestamp = time;
    }

    public void add_filename(String string){
        filename = string;
    }

    public String get_filename(){
        return filename;
    }

    public long get_timestamp(){
        return timestamp;
    }


}
