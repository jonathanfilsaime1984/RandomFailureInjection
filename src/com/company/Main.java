package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main
{
    public static void main(String[] args) throws Exception {
        //modify rabbitmq-env.conf file
        modifyPortNumber(5675);
        System.out.println("----------------------------------------------------------");

        //unpack the toxiproxy server and cli from the jar
        unpackToxiProxyServer();
        unpackToxiProxyCli();
        Thread.sleep(100000);
        //list of commands the be executed sequencially by the bash shell
        ArrayList<String[]> cmd = new ArrayList<>();
        cmd.add(new String[]{"/bin/sh", "-c", "pkill -f rabbit"});
        cmd.add(new String[]{"/bin/sh", "-c", "sudo rabbitmq-server"});
        cmd.add(new String[]{"/bin/sh", "-c", "chmod 777 toxiproxy-server-linux-amd64.exe"});
        cmd.add(new String[]{"/bin/sh", "-c", "chmod 777 toxiproxy-cli-linux-amd64.exe"});
        cmd.add(new String[]{"/bin/sh", "-c", "cd /home/jonathanfilsaime/Desktop/SeniorProject && ./toxiproxy-server-linux-amd64.exe"});
        cmd.add(new String[]{"/bin/sh", "-c", "cd /home/jonathanfilsaime/Desktop/SeniorProject && ./toxiproxy-cli-linux-amd64.exe create test -l localhost:5672 -u localhost:5675"});
        cmd.add(new String[]{"/bin/sh", "-c", "echo \"WITH toxiproxy-cli toxic add test -t latency -a latency=2000\" >> /Users/jonathan/Desktop/messageTest.txt"});
        cmd.add(new String[]{"/bin/sh", "-c", "cd /home/jonathanfilsaime/Desktop/SeniorProject && ./toxiproxy-cli-linux-amd64.exe toxic add test -t latency -a latency=200"});           //toxic added
        cmd.add(new String[]{"/bin/sh", "-c", "cd /home/jonathanfilsaime/Desktop/SeniorProject && java -cp rabbit1.jar com.hpe.oneview.Application >> /home/jonathanfilsaime/Desktop/SeniorProject/messageTest.txt"});  //launch rabbit messaging program
        cmd.add(new String[]{"/bin/sh", "-c", "echo \"\ntest------------------------------------------\n\" >> /home/jonathanfilsaime/Desktop/SeniorProject/messageTest.txt"});

        Process process = null;

        //printing the output of each thread to the screen
        for(int i = 0; i < cmd.size() ; i++)
        {
            process = Runtime.getRuntime().exec(cmd.get(i));
            BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            new Thread(new InputReader(processReader)).start();

            /*time delay is important because the servers my not be fully up in
            running before executing the next command*/
            Thread.sleep(10000);
        }

//        failureInjection(2000);
//        launchProgram();
    }

    //this method is used the modify the rabbitmq-env.conf file
    public static void modifyPortNumber(int portNumber)
    {
        try
        {
            //modify rabbitmq-env.conf file
            File file = new File("/etc/rabbitmq/rabbitmq-env.conf");
            FileWriter replace = new FileWriter(file, false);
            replace.write("CONFIG_FILE=/usr/local/etc/rabbitmq/rabbitmq\n" + "NODE_IP_ADDRESS=127.0.0.1\n" + "NODENAME=rabbit@localhost\n" + "NODE_PORT=" + portNumber);
            replace.close();

            //print out the changes made to the file
            File newFile = new File("/etc/rabbitmq/rabbitmq-env.conf");
            FileReader fr = new FileReader(newFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while( (line = br.readLine()) != null )
            {
                System.out.println(line);
            }
            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void failureInjection(int latency) throws IOException, InterruptedException
    {
        Process failure = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "toxiproxy-cli toxic add test -t latency -a latency=" + latency });
        BufferedReader processReader = new BufferedReader(new InputStreamReader(failure.getInputStream()));
        new Thread(new InputReader(processReader)).start();
    }

    public static void launchProgram()throws IOException, InterruptedException
    {
        Process launch = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd  /Users/jonathan/IdeaProjects/rabbit1/out/artifacts/rabbit1_jar && java -cp rabbit1.jar com.hpe.oneview.Application"});
        BufferedReader processReader = new BufferedReader(new InputStreamReader(launch.getInputStream()));
        new Thread(new InputReader(processReader)).start();
    }

    public static void unpackToxiProxyServer() throws Exception
    {
        File file = new File("RandomFailureInjection.jar");
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

        for (ZipEntry e; (e = zis.getNextEntry()) != null; ) {
            if (e.getName().equals("toxiproxy-server-linux-amd64")) {
                System.out.println("Found it");
                break;
            }
        }

        PrintStream ps = new PrintStream(new File("toxiproxy-server-linux-amd64.exe"));
        byte[] buffer = new byte[4096];
        int length;
        while ((length = zis.read(buffer)) != -1)
        {
            System.out.println(length);
            ps.write(buffer, 0, length);
        }
        System.out.println("Finished writing file.");
        ps.flush();
        ps.close();
    }

    public static void unpackToxiProxyCli() throws Exception
    {
        File file = new File("RandomFailureInjection.jar");
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

        for (ZipEntry e; (e = zis.getNextEntry()) != null; ) {
            if (e.getName().equals("toxiproxy-cli-linux-amd64")) {
                System.out.println("Found it");
                break;
            }
        }

        PrintStream ps = new PrintStream(new File("toxiproxy-cli-linux-amd64.exe"));
        byte[] buffer = new byte[4096];
        int length;
        while ((length = zis.read(buffer)) != -1) {
            System.out.println(length);
            ps.write(buffer, 0, length);
        }
        System.out.println("Finished writing file.");
        ps.flush();
        ps.close();

    }
}



//each thread runs a different commands
class InputReader implements Runnable
{
    private final BufferedReader br;
    public InputReader(final BufferedReader br)
    {
        this.br = br;
    }

    @Override
    public void run()
    {
        String line;
        try
        {
            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
            System.out.println("----------------------------------------------------------");
        }
        catch(IOException e) {}
    }
}

