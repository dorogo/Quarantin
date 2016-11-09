/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quarantinelab;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class QuarantineLab {

    private static volatile int i = 0;
    private static volatile int le = 0;
    private static String pathSrcText = System.getProperty("user.dir") + "/packet.txt";
    private static String pathCleanText = System.getProperty("user.dir") + "/packet2.txt";

    private static volatile char c;
    private static volatile Character ch;
    private static volatile Queue<Character> fifo = new LinkedList<Character>();
//    private static volatile Queue<Integer> fifo = new LinkedList<Integer>();
    private static volatile boolean isLive = true;
    private static final boolean test = true;
//    private static Stack fifo = new Stack();

    private static final Object monitor = new Object();

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Thread myThread1 = new Thread(new Runnable() {
            String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            

            public void run() //Этот метод будет выполняться в побочном потоке
            {
                try {
                    s = FileHelper.getString(pathCleanText);
                } catch (IOException ex) {
                    Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                }
                le = s.length();
//                System.out.println("" + Thread.currentThread());
                synchronized (monitor) {
                    monitor.notify();
                }
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                while (isLive) {
                    if (i < s.length()) {
                        try {
                            s = FileHelper.getString(pathCleanText);
                        } catch (IOException ex) {
                            Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        ch = new Character(s.charAt(i));
//                        System.out.println("1" + Thread.currentThread());
                        synchronized (monitor) {
                            fifo.add(ch);                            
                        }
                    }
                    i++;

                    Thread.yield();
                }
            }
        });

        Thread myThread2 = new Thread(new Runnable() {
            Character tmp;
            String s = "";
            String result;

            public void run() //Этот метод будет выполняться в побочном потоке
            {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                synchronized (monitor) {
                    monitor.notify();
                }
                System.out.println("2- " + System.currentTimeMillis());
                
                while (isLive) {
                    System.out.println(""+fifo.size());
                    try {
                        s = FileHelper.getString(pathCleanText);
                    } catch (IOException ex) {
                        Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    tmp = ch;
//                    System.out.println("" + tmp + i);
                    synchronized(monitor) {
                            if (fifo.size() > 0) {
                                result += fifo.poll().toString();
                            }
                        }
                    if (i < le && tmp != null) {
                        
//                        result += tmp.toString();
                    } else if (i == le) {
                        FileHelper.writeString(result, (System.getProperty("user.dir") + "/packet212312.txt"), false);
                    } else if (i > le && fifo.size() == 0) {
                        System.out.println("" + fifo.size() + " " + s.length());
                        System.out.println("res=" + result.length() + " src=" + le + " " + result);
                        System.out.println("" + ((float) result.length() / (float) le));
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//                    System.out.println("2" + Thread.currentThread());
                    Thread.yield();
                }
            }
        });

        myThread2.start();	//Запуск потока
        myThread1.start();	//Запуск потока
//        }
//        System.out.println("olololool");
//        FileHelper.writeString(generateString(new SecureRandom(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789       !@#$%^&*_-+=", 7008), (System.getProperty("user.dir") + "/packet.txt"), false);
//        String tmp = FileHelper.getString( (System.getProperty("user.dir") + "/packet.txt"));
//        String result = "";
//        int l = tmp.length();
//        char c;
//        for (int j = 0; j < l; j++) {
//            c = tmp.charAt(j);
//            if (Character.toString(c).matches("[A-Za-z0-9 ]")) {
//                result += c;
//            }
//        }
//        System.out.println(tmp.length() + " - "+result.length());
//        FileHelper.writeString(result, (System.getProperty("user.dir") + "/packet2.txt"), false);

    }

    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

}
