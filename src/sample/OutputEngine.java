package sample;
import javafx.application.Platform;

import java.util.Iterator;

import static sample.Controller.*;
import static sample.ProducerConsumer.*;

public class OutputEngine implements Runnable {
    @Override
    public void run() {
        while(true){

           // when search is done, begin to print
            if(isDone){
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           //     System.out.println("service is shut down" + service.isShutdown());
                this.printResults();


            }

        }
    }

    /**
     * print result to textarea and count label using binding
     */
    public void printResults() {


                try {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            fileCount.set(0,Integer.toString(atomicCount.get()) );
                        //    System.out.println("fileList size " + fileList.size());
                            String files = "";
                            // keep thread safe
                            if(!syncalFileList.isEmpty()){
                                synchronized(syncalFileList) {
                                    Iterator<String> iterator = syncalFileList.iterator();
                                    while (iterator.hasNext())

                                        files = files +" " + iterator.next()+"\n";
                                }
                            }

                            filesOutput.set(0,files);

                            return;
//

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;

        }
    }

