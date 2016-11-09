/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quarantinelab;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class QuarantineLab {

    private static volatile int i = 0;
    private static volatile int le = 0;
    private static String pathSrcText = System.getProperty("user.dir") + "/srcDirtyText.txt";
    private static String pathCleanSrcText = System.getProperty("user.dir") + "/srcCleanText.txt";
    private static String pathCleanText = System.getProperty("user.dir") + "/resultCleanText.txt";

    private static volatile Character ch;
    private static volatile Queue<Character> fifo = new LinkedList<Character>();
    private static volatile boolean isLive = true;
    private static final boolean test = true;

    private static final Object monitor = new Object();
    
    
    private static boolean isBuffered = true;

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Thread myThread1 = new Thread(new Runnable() {
            String s = "";

            public void run() //Этот метод будет выполняться в побочном потоке
            {
                try {
                    s = FileHelper.getString(pathSrcText);
                } catch (IOException ex) {
                    Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                }
                le = s.length();
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
                            s = FileHelper.getString(pathSrcText);
                        } catch (IOException ex) {
                            Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        ch = new Character(s.charAt(i));
                        synchronized (monitor) {
                            if (isBuffered) //для режима с буфером
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

                while (isLive) {
                    try {
                        s = FileHelper.getString(pathSrcText);
                    } catch (IOException ex) {
                        Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    tmp = ch;
                    //принимаем символы из буфера и отсеиваем ненужные символы
                    synchronized (monitor) {
                        if (isBuffered && fifo.size() > 0) {
                            if (fifo.peek().toString().matches("[A-Za-z0-9 ]")) {
                                if (result == null) {
                                    result = fifo.poll().toString();
                                } else {
                                    result += fifo.poll().toString();
                                }
                            } else {
                                fifo.poll();
                            }
                        }
                    }
                    if (!isBuffered && i < le && tmp != null) {
                        if (tmp.toString().matches("[A-Za-z0-9 ]"))
                            result += tmp.toString();
                    } else if (i > le && ( fifo.size() == 0 || !isBuffered)) {
                        //запись чистого текста и проверкуа на потери
                        FileHelper.writeString(result, pathCleanText, false);
//                        System.out.println("" + fifo.size() + " " + s.length());
                        try {
                            System.out.println("res=" + result.length() + " src=" + FileHelper.getString(pathCleanSrcText).length() + " " + result);
                            System.out.println("" + ((float) result.length() / (float)FileHelper.getString(pathCleanSrcText).length() * 100) + "%");
                        } catch (IOException ex) {
                            Logger.getLogger(QuarantineLab.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        isLive = false;
                    }
                    Thread.yield();
                }
            }
        });

        myThread2.start();	//Запуск потока
        myThread1.start();	//Запуск потока
        
        //для формирования исходного файла
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
