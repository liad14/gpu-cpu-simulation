package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.util.LinkedList;
import java.util.List;

public class main {

    public static void main(String[] args) {
        Student stud1 = new Student("stud1", "computers", Student.Degree.PhD);
        Student stud2 = new Student("stud2", "computers", Student.Degree.MSc);
        Student stud3 = new Student("stud3", "computers", Student.Degree.PhD);
        for (Integer j = 15; j < 25; j++) {
            String NameMod1 = "model-" + j.toString() + " of stud1";
            String NameMod2 = "model-" + j.toString() + " of stud2";
            String NameMod3 = "model-" + j.toString() + " of stud3";
            stud1.addModel(new Model(NameMod1, Data.Type.Tabular, j * 1000,stud1));
            stud2.addModel(new Model(NameMod2, Data.Type.Tabular, j * 1000,stud2));
            stud3.addModel(new Model(NameMod3, Data.Type.Images, j * 1000,stud3));
        }
        List<GPU> GPUS=new LinkedList<>();
        GPUS.add(new GPU(GPU.Type.RTX2080)) ;
        GPUS.add(new GPU(GPU.Type.RTX3090)) ;
        GPUS.add(new GPU(GPU.Type.GTX1080));
        GPUS.add(new GPU(GPU.Type.RTX3090)) ;
        GPUS.add(new GPU(GPU.Type.RTX2080)) ;
        GPUS.add(new GPU(GPU.Type.RTX2080)) ;
        List<CPU> CPUS=new LinkedList<>();
        CPUS.add(new CPU(32));
        CPUS.add(new CPU(4));
        CPUS.add(new CPU(8));
        CPUS.add(new CPU(16));
        CPUS.add(new CPU(32));
        CPUS.add(new CPU(32));
        List<ConfrenceInformation> conferences=new LinkedList<>();
//        conferences.add(new ConfrenceInformation("conference1",1000));
//        conferences.add(new ConfrenceInformation("conference2",1500));
//        conferences.add(new ConfrenceInformation("conference3",2000));
        conferences.add(new ConfrenceInformation("conference4",2000));
        conferences.add(new ConfrenceInformation("conference5",1500));
        conferences.add(new ConfrenceInformation("conference6",1000));
        List<StudentService> studentServices=new LinkedList<>();
        List<GPUService> GPUServices=new LinkedList<>();
        List<CPUService> CPUServices=new LinkedList<>();
        List<ConferenceService> ConfServices=new LinkedList<>();
        StudentService s1=new StudentService("studService 1");
        StudentService s2=new StudentService("studService 2");
        StudentService s3=new StudentService("studService 3");
        s1.setStudent(stud1);
        s2.setStudent(stud2);
        s3.setStudent(stud3);
        Thread t1=new Thread(s1);
        Thread t2=new Thread(s2);
        Thread t3=new Thread(s3);
        studentServices.add(s1);
        studentServices.add(s2);
        studentServices.add(s3);
        Integer name=1;
        for(GPU g:GPUS){
            String myName="GPU "+ name.toString();
            GPUService gS=new GPUService(myName);
            GPUServices.add(gS);
            gS.setMyGPU(g);
            name++;
            Thread t=new Thread(gS);
            t.start();
        }
        name=1;
        for(CPU g:CPUS){
            String myName="CPU "+ name.toString();
            CPUService gS=new CPUService(myName);
            CPUServices.add(gS);
            gS.setCPU(g);
            name++;
            Thread t=new Thread(gS);
            t.start();
        }
        name=1;
        for(ConfrenceInformation g:conferences){
            String myName="Conference "+ name.toString();
            ConferenceService gS=new ConferenceService(myName);
            ConfServices.add(gS);
            gS.setConference(g);
            name++;
            Thread t=new Thread(gS);
            t.start();
        }
        boolean StudAreAllInit=false;
        boolean GPUAreAllInit=false;
        boolean CPUAreAllInit=false;
        boolean ConfAreAllInit=false;
        boolean MicrosAreAllInit=false;
        while (!MicrosAreAllInit){
            if(!GPUAreAllInit){
                int GPUCounter=0;
                for(GPUService s:GPUServices){
                    if(s.isInit())
                        GPUCounter++;
                    if(GPUCounter==GPUServices.size())
                        GPUAreAllInit=true;
                }
            }
            if(!CPUAreAllInit){
                int CPUCounter=0;
                for(CPUService s:CPUServices){
                    if(s.isInit())
                        CPUCounter++;
                    if(CPUCounter==CPUServices.size())
                        CPUAreAllInit=true;
                }
            }
            if(!ConfAreAllInit){
                int ConfiCounter=0;
                for(ConferenceService s:ConfServices){
                    if(s.isInit())
                        ConfiCounter++;
                    if(ConfiCounter==ConfServices.size())
                        ConfAreAllInit=true;
                }
            }
            MicrosAreAllInit = GPUAreAllInit && ConfAreAllInit && ConfAreAllInit;
        }
        t1.start();
        t2.start();
        t3.start();
        while(!StudAreAllInit){
            int studCounter=0;
            for(StudentService s:studentServices){
                if(s.isInit())
                    studCounter++;
                if(studCounter==studentServices.size())
                    StudAreAllInit=true;
            }
        }
        TimeService timer=new TimeService(20,2000);
        Thread t=new Thread(timer);
        t.start();
    }


}
