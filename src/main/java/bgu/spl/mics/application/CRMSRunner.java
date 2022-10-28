package bgu.spl.mics.application;
import bgu.spl.mics.application.FileToOut;
import bgu.spl.mics.application.InputFiles.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import static java.lang.String.valueOf;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) throws IOException, InterruptedException {

        Gson gsonFile = new Gson();
        InputFile newInput = null;
        try (Reader newReader = new FileReader(args[0])) {
            newInput = gsonFile.fromJson(newReader, InputFile.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error in input file.");
            e.printStackTrace();
        }
        List<Student> studentList = new LinkedList<>();
        List<StudentService> studentServiceList = new LinkedList<>();
        List<Thread> studentServiceThreadsList = new LinkedList<>();
        int studentServiceName = 0;
        for (InputStudent studentToAdd : newInput.getStudents()) {
            Student.Degree status = null;
            if (studentToAdd.getStatus() == "Phd")
                status = Student.Degree.PhD;
            else
                status = Student.Degree.MSc;
            Student newStudent = new Student(studentToAdd.getName(), studentToAdd.getDepatrtment(), status);
            newStudent.setDepartment(studentToAdd.getDepatrtment());

            for (InputModel modelToAdd : studentToAdd.getModels()) {
                Data.Type type = null;
                if (modelToAdd.getType() == "Images")
                    type = Data.Type.Images;
                if (modelToAdd.getType() == "Text")
                    type = Data.Type.Text;
                else type = Data.Type.Tabular;
                Model newModel = new Model(modelToAdd.getName(), type, modelToAdd.getSize(), newStudent);
                newStudent.addModel(newModel);
            }
            studentList.add(newStudent);
            StudentService newStudentService = new StudentService("studentService " + studentServiceName);
            newStudentService.setStudent(newStudent);
            studentServiceList.add(newStudentService);

            Thread studentServiceThread = new Thread(newStudentService);//TODO threads names?
            studentServiceThreadsList.add(studentServiceThread);
            studentServiceName++;

        }
        List<GPU> gpuList = new LinkedList<>();
        List<GPUService> GPUServicesList = new LinkedList<>();
        Integer GpuName = 0;
        for (String gpuToAdd : newInput.getGPUS()) {
            GPU newGpu = new GPU(GPU.Type.valueOf(gpuToAdd));
            gpuList.add(newGpu);
            String myName = "GPU " + GpuName.toString();
            GPUService gS = new GPUService(myName);
            GPUServicesList.add(gS);
            gS.setMyGPU(newGpu);
            GpuName++;
            Thread t = new Thread(gS);
            t.start();
        }
        List<CPU> cpuList = new LinkedList<>();
        List<CPUService> CPUServicesList = new LinkedList<>();
        Integer CpuName = 0;
        for (int cpuToAdd : newInput.getCPUS()) {
            CPU newCpu = new CPU(cpuToAdd);
            cpuList.add(newCpu);

            String myName = "CPU " + CpuName.toString();
            CPUService cpuService = new CPUService(myName);
            CPUServicesList.add(cpuService);
            cpuService.setCPU(newCpu);
            CpuName++;

            Thread t = new Thread(cpuService);
            t.start();
        }
        List<ConfrenceInformation> coneferenceInformationList = new LinkedList<>();
        List<ConferenceService> conferenceServiceList = new LinkedList<>();
        Integer conferenceName = 0;

        for (InputConference conferenceToAdd : newInput.getConferences()) {
            ConfrenceInformation newConference = new ConfrenceInformation(conferenceToAdd.getName(), conferenceToAdd.getDate());
            coneferenceInformationList.add(newConference);

            String myName = "Conference " + conferenceName;
            ConferenceService newConferenceService = new ConferenceService(myName);
            conferenceServiceList.add(newConferenceService);
            newConferenceService.setConference(newConference);
            conferenceName++;

            Thread t = new Thread(newConferenceService);
            t.start();
        }
        //------------------check CPUs GPUs Confs were all initialized---------------------------------------------- // TODO to check that the ticks are sended according to the input file inputTick
        Boolean allGpusInitialized = false;
        Boolean allCpusInitialized = false;
        Boolean allConferencesInitialized = false;
        Boolean allSystemMicrosInitialized = false;

        //TODO sum of papers reads supposed to ce the same as the others publications
        //TODO cleaning of the messageBus each test

        while (!allSystemMicrosInitialized) {
            //-----------------------check GPUs all initialized------------------------
            if (!allGpusInitialized) {
                int initilizedGPU = 0;
                for (GPUService serviceToInit : GPUServicesList) {
                    if (serviceToInit.isInit()) {
                        initilizedGPU++;
                    }
                    if (initilizedGPU == GPUServicesList.size()) {
                        allGpusInitialized = true;
                    }
                }
            }  //-----------------------check CPUs all initialized------------------------
            if (!allCpusInitialized) {
                int initilizedCPU = 0;
                for (CPUService serviceToInit : CPUServicesList) {
                    if (serviceToInit.isInit()) {
                        initilizedCPU++;
                    }
                    if (initilizedCPU == CPUServicesList.size()) {
                        allCpusInitialized = true;
                    }
                }
            }  //-----------------------check Conferences all initialized------------------------
            if (!allConferencesInitialized) {
                int initializedConferences = 0;
                for (ConferenceService serviceToInit : conferenceServiceList) {
                    if (serviceToInit.isInit()) {
                        initializedConferences++;
                    } else
                    if (initializedConferences == conferenceServiceList.size()) {
                        allConferencesInitialized = true;
                }
            }
            if (allGpusInitialized && allCpusInitialized && allConferencesInitialized) {
                allSystemMicrosInitialized = true;
            }
        }
        for (Thread studentServiceThread : studentServiceThreadsList) {
            studentServiceThread.start();

        }
        Boolean allStudentsInitialized = false;
        while (!allStudentsInitialized) {
            int initilizedStudents = 0;
            for (StudentService serviceToInit : studentServiceList) {
                if (serviceToInit.isInit()) {
                    initilizedStudents++;
                }
                if (initilizedStudents == studentServiceList.size()) {
                    allStudentsInitialized = true;
                }
            }
        }

        TimeService newTimeService = new TimeService(newInput.getTickTime(), newInput.getDuration());
        Thread timeThread = new Thread(newTimeService);
        try {
            timeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            timeThread.join();
        } catch (Exception e) {
        }

        // --------------------------check all microServicesWereTerminated--------------------------
        Boolean allGpusTerminated = false;
        Boolean allCpusTerminated = false;
        Boolean allConferencedTerminated = false;
        Boolean allStudentsTerminated = false;
        Boolean allMicrosTerminated = false;

        while (!allMicrosTerminated) {
            //-----------------------check all GPUs terminated------------------------
            if (!allGpusTerminated) {
                int terminatedGPU = 0;
                for (GPUService serviceToTerminate : GPUServicesList) {
                    if (serviceToTerminate.isTerminated())
                        terminatedGPU++;
                    if (terminatedGPU == GPUServicesList.size())
                        allGpusTerminated = true;
                }
            }  //-----------------------check all CPUs terminated------------------------
            if (!allCpusTerminated) {
                int TerminatedCPU = 0;
                for (CPUService serviceToTerminate : CPUServicesList) {
                    if (serviceToTerminate.isTerminated())
                        TerminatedCPU++;
                    if (TerminatedCPU == CPUServicesList.size())
                        allCpusTerminated = true;
                }
            }  //-----------------------check all Conferences terminated------------------------
            if (!allConferencedTerminated) {
                int TerminatedConferences = 0;
                for (ConferenceService serviceToTerminate : conferenceServiceList) {
                    if (serviceToTerminate.isTerminated())
                        TerminatedConferences++;
                    if (TerminatedConferences == conferenceServiceList.size())
                        allConferencedTerminated = true;
                }
            }   //-----------------------check all Students terminated------------------------

            if (!allStudentsTerminated) {
                int TerminatedStudents = 0;
                for (StudentService serviceToTerminate : studentServiceList) {
                    if (serviceToTerminate.isTerminated())
                        TerminatedStudents++;
                    if (TerminatedStudents == studentServiceList.size())
                        allStudentsTerminated = true;
                }
                if (allGpusTerminated && allCpusTerminated && allConferencedTerminated && allStudentsTerminated)
                    allMicrosTerminated = true;
            }
        }


        //---------------------------------Output-----------------------------------------------------
        FileToOut.getInstance();
        int CPUTimeUsed = 0;
        for (CPU cpuToAccumulate : cpuList) {
            CPUTimeUsed = CPUTimeUsed + (cpuToAccumulate.getCurrTick() * newInput.getTickTime());
        }
        System.out.println(CPUTimeUsed);

        int GPUTimeUsed = 0;
        for (GPU gpuToAccumulate : gpuList) {
            GPUTimeUsed += (gpuToAccumulate.getCurTick() * newInput.getTickTime());
        }
        System.out.println(GPUTimeUsed);
        FileToOut.getInstance().makeOutputFile(CPUTimeUsed, GPUTimeUsed);
    }
}