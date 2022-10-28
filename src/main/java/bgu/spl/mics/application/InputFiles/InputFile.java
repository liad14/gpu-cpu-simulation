package bgu.spl.mics.application.InputFiles;

public class InputFile {

    //-------------------fields-------------------------
     InputStudent[] Students;
     String[] GPUS;
     int[] CPUS;
     InputConference[] Conferences;
     int TickTime;
     int Duration;

    //------------------Constructor----------------------

    public InputStudent[] getStudents() {
        return Students;
    }
    public void setStudents(InputStudent[] students) {
        Students = students;
    }
    public String[] getGPUS() {
        return GPUS;
    }
    public int[] getCPUS() {
        return CPUS;
    }
    public InputConference[] getConferences() {
        return Conferences;
    }
    public int getTickTime() {
        return TickTime;
    }
    public int getDuration() {
        return Duration;
    }

}

