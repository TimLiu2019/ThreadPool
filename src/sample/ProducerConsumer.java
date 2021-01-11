package sample;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static sample.Controller.*;


public class ProducerConsumer {
    // use this variable test many times, .*\.txt, count: the result is unstable, sometimes 82, sometimes 81, 79
    volatile static int count = 0;
    // file count
    volatile static AtomicInteger atomicCount = new AtomicInteger();

    //   results
    volatile static List<String> syncalFileList =
            Collections.synchronizedList(new ArrayList<String>());
    // this may cause errors when  clicking search quickly and constantly
    volatile static List<String> fileList = new ArrayList<>();



    public static void startIndexing() {
        count = 0;
        atomicCount.set(0);
        BlockingQueue<File> fileQueue = new LinkedBlockingQueue<File>(1000);
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().matches(fileName)) {
                    return true;
                }
                return false;
            }
        };


        // set up 11 producers and 8 consumers
        ExecutorService service = Executors.newFixedThreadPool(8);
        Thread crawlerThread0 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath)));
        Thread crawlerThread1 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Canada")));
        Thread crawlerThread2 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/USA")));
        Thread crawlerThread3 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Mexico")));
        Thread crawlerThread4 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Brazil")));
        Thread crawlerThread5 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Argentina")));
        Thread crawlerThread6 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Scotland")));
        Thread crawlerThread7 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/England")));
        Thread crawlerThread8 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Germany")));
        Thread crawlerThread9 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/France")));
        Thread crawlerThread10 = new Thread(new FileCrawler(fileQueue, fileFilter, new File(startPath + "/Italy")));
        Thread indexerThread1 = new Thread(new Indexer(fileQueue));
        Thread indexerThread2 = new Thread(new Indexer(fileQueue));
        Thread indexerThread3 = new Thread(new Indexer(fileQueue));
        Thread indexerThread4 = new Thread(new Indexer(fileQueue));
        Thread indexerThread5 = new Thread(new Indexer(fileQueue));
        Thread indexerThread6 = new Thread(new Indexer(fileQueue));
        Thread indexerThread7 = new Thread(new Indexer(fileQueue));
        Thread indexerThread8 = new Thread(new Indexer(fileQueue));
        service.execute(crawlerThread0);
        service.execute(crawlerThread1);
        service.execute(crawlerThread2);
        service.execute(crawlerThread3);
        service.execute(crawlerThread4);
        service.execute(crawlerThread5);
        service.execute(crawlerThread6);
        service.execute(crawlerThread7);
        service.execute(crawlerThread8);
        service.execute(crawlerThread9);
        service.execute(crawlerThread10);

        // very interesting, if only use one consumer thread, the search result is not right
        service.execute(indexerThread1);
        service.execute(indexerThread2);
        service.execute(indexerThread3);
        service.execute(indexerThread4);
        service.execute(indexerThread5);
        service.execute(indexerThread6);
        service.execute(indexerThread7);
        service.execute(indexerThread8);

        // service.shutdown() is not shutdown immediately, it stops accepting new tasks

        service.shutdown();

    }

    static class FileCrawler implements Runnable {
        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;
        // top root, in this folder, files only find in one layer, no going deep
        private final File roots = new File(startPath);


        public FileCrawler(BlockingQueue<File> fileQueue,
                           final FileFilter fileFilter,
                           File root) {
            this.fileQueue = fileQueue;
            this.root = root;
            this.fileFilter = new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || fileFilter.accept(f);
                }
            };
        }

        private boolean alreadyIndexed(File f) {
            //  see if this component has already been indexed
            return fileQueue.contains(f);
        }

        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawl(File root) throws InterruptedException {
            File[] entries = root.listFiles(fileFilter);
            if (entries != null) {
                // if producer thread is 1 - 10, go deep to bottom folders
                if (!root.getName().equals(roots.getName())) {
                    for (File entry : entries) {
                        if (entry.isDirectory()) {
                            crawl(entry);
                        } else if (!alreadyIndexed(entry)) {
                            fileQueue.put(entry);

                         //   fileList.add(entry.getAbsolutePath());
                            syncalFileList.add(entry.getAbsolutePath());

                        }
                    }
                    // if producer thread is 0, just search one layer
                } else if (root.getName().equals(roots.getName())) {
                    for (File entry : entries)
                        if (!alreadyIndexed(entry) && !entry.isDirectory()) {
                            fileQueue.put(entry);
                         //   fileList.add(entry.getAbsolutePath());
                            syncalFileList.add(entry.getAbsolutePath());


                        }

                }
            }

        }
    }

    static class Indexer implements Runnable {
        private final BlockingQueue<File> queue;

        public Indexer(BlockingQueue<File> queue) {
            this.queue = queue;
        }

        /**
         * Change take() to poll()
         * take() makes blocking
         */
        public void run() {
            try {
                while (true) {
                    File f = queue.poll(200, TimeUnit.MILLISECONDS);
                    if (f != null) {

                        indexFile(f);

                    }

                    try {
                        Thread.sleep(100);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // if producer finished and queue is empty, time to finish
                    if (queue.isEmpty()) {

                        // System.out.println("consumer done .........");
                        //System.out.println("Total: " + count + " files");
                        isDone = true;


                        return;
                    }

                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void indexFile(File file) {
            // Index the file...
            //   System.out.println(file.getName());
            //  count++;
            atomicCount.incrementAndGet();
            //    System.out.println("count: "+count);


        }

        ;
    }

    private static final int BOUND = 10;
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();

//    public static void startIndexing(File[] roots) {
//        BlockingQueue<File> queue = new LinkedBlockingQueue<File>(BOUND);
//        FileFilter filter = new FileFilter() {
//            public boolean accept(File file) {
//                return true;
//            }
//        };
//
//        for (File root : roots)
//            new Thread(new FileCrawler(queue, filter, root)).start();
//
//        for (int i = 0; i < N_CONSUMERS; i++)
//            new Thread(new Indexer(queue)).start();
//    }
}
