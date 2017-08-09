package SPDownloader;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static SPTools.Extractor.getFile;
import static SPTools.Extractor.getJarURI;

class Downloader {

    static Thread t;
    static boolean isDownloading = false;
    private static String toConsole = "";

    static void downloader(CheckBox languageGerman, CheckBox languageEnglish, ArrayList<String> selectedEpisodes, TextArea console, SplitPane upperSplit) {
        int threadsInt = 1;

        //threadsInt = (int) threads.getSelectionModel().getSelectedItem();

        t = new Thread(() -> {
            isDownloading = true;
            int languageInt = 0;
            boolean multiLang = false;
            String languageStr = "";
            if (languageGerman.isSelected() && languageEnglish.isSelected()) {
                multiLang = true;
                languageStr = "German + English";
            } else {
                if (languageGerman.isSelected()) {
                    languageInt = 1;
                    languageStr = "German";
                } else if (languageEnglish.isSelected()) {
                    languageInt = 0;
                    languageStr = "English";
                }
            }
            for (String selectedEpisode : selectedEpisodes) {

                String currEpisode = Arrays.asList(selectedEpisode.split(",")).get(0);
                String currSeason = Arrays.asList(selectedEpisode.split(",")).get(1).replace("Season ", "");
                String videoURL = null;
                ArrayList<String> videoURLS = new ArrayList<>();
                if (multiLang) {
                    videoURLS.add("http://southpark.cc.com/full-episodes/s" + currSeason + "e" + currEpisode);
                    videoURLS.add("http://southpark.de/alle-episoden/s" + currSeason + "e" + currEpisode);
                } else {
                    if (languageInt == 0) {
                        videoURL = "http://southpark.cc.com/full-episodes/s" + currSeason + "e" + currEpisode;
                        System.out.println(videoURL);
                    } else {
                        videoURL = "http://southpark.de/alle-episoden/s" + currSeason + "e" + currEpisode;
                        System.out.println(videoURL);
                    }
                }


                Path workingDirectory = Paths.get("").toAbsolutePath();
                try {
                    if (!multiLang) {
                        final URI uriYTDL;
                        final URI exeYTDL;

                        uriYTDL = getJarURI();
                        exeYTDL = getFile(uriYTDL, "res/youtube-dl.exe");
                        Process process = new ProcessBuilder(new File(exeYTDL).getAbsolutePath(), videoURL, "--ignore-errors", "--retries", "10", "--output", "\"" + workingDirectory.toString() + "\\temp%(title)s.%(ext)s\"").start();
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        String stdOut = null;
                        while ((line = br.readLine()) != null) {
                            if (line.toLowerCase().contains("downloading playlist")) {
                                String[] result = line.substring(line.indexOf("playlist") + 10, line.length()).split("\n", 2);
                                if (new File("SouthPark " + currSeason + "." + currEpisode + " - " + result[0] + " (" + languageStr + ").mkv").exists()) {
                                    process.destroy();
                                    console.appendText("SouthPark " + currSeason + "." + currEpisode + " - " + result[0] + " (" + languageStr + ").mkv" + " already exists! Going to next file...");
                                    break;
                                }
                            }
                            toConsole = line;
                            Platform.runLater(() -> {
                                if (!toConsole.equals("")) {
                                    console.appendText(toConsole + "\n");
                                }
                            });
                            stdOut = stdOut + line + "\n";
                            if (line.toLowerCase().contains("[download] finished downloading playlist:")) {
                                String lastFileName = stdOut.substring(stdOut.indexOf("playlist") + 10, stdOut.length());

                                String[] result = lastFileName.split("\n", 2);

                                lastFileName = result[0];

                                System.out.println(result[0]);

                                Process process2 = null;
                                File[] dats = finder(new File(".").getCanonicalPath(), ".mp4");
                                try {
                                    if (lastFileName.contains("&amp;")) {
                                        lastFileName = lastFileName.replaceAll("&amp;", "and");
                                    }
                                    if (lastFileName.contains(":")) {
                                        lastFileName = lastFileName.replaceAll(":", "");
                                    }
                                    if (lastFileName.contains("&")) {
                                        lastFileName = lastFileName.replaceAll("&", "and");
                                    }
                                    if (lastFileName.contains("\"")) {
                                        lastFileName = lastFileName.replaceAll("\"", "");
                                    }
                                    String arg = "\"SouthPark " + currSeason + "." + currEpisode + " - " + lastFileName + " (" + languageStr + ").mkv\"";
                                    String arg2 = "";
                                    for (int i = 0; i < dats.length; i++) {
                                        if (dats.length - 1 == i) {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                        } else {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                            arg2 = arg2 + " + ";
                                        }
                                    }
                                    final URI uriMKVM;
                                    final URI exeMKVM;

                                    uriMKVM = getJarURI();
                                    exeMKVM = getFile(uriMKVM, "res/mkvmerge.exe");
                                    process2 = new ProcessBuilder(new File(exeMKVM).getAbsolutePath(), "-o", arg, arg2).start();
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }
                                InputStream is2 = process2.getInputStream();
                                InputStreamReader isr2 = new InputStreamReader(is2);
                                BufferedReader br2 = new BufferedReader(isr2);
                                String line2;

                                try {
                                    while ((line2 = br2.readLine()) != null) {
                                        toConsole = line2;
                                        System.out.println(line2);
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!toConsole.equals("")) {
                                                    console.appendText(toConsole + "\n");
                                                }
                                            }
                                        });
                                    }
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }

                                for (File files : dats) {
                                    try {
                                        Files.delete(Paths.get(files.getAbsolutePath()));
                                    } catch (IOException ex) {
                                        StringWriter errors = new StringWriter();
                                        ex.printStackTrace(new PrintWriter(errors));
                                        JOptionPane.showMessageDialog(null, "" + errors.toString());
                                    }
                                }
                            }
                            System.out.println(line);
                        }
                    } else {

                        final URI uriYTDL;
                        final URI exeYTDL;

                        uriYTDL = getJarURI();
                        exeYTDL = getFile(uriYTDL, "res/youtube-dl.exe");
                        Process process = new ProcessBuilder(new File(exeYTDL).getAbsolutePath(), videoURLS.get(0), "--ignore-errors", "--retries", "10", "--output", "\"" + workingDirectory.toString() + "\\temp%(title)s.%(ext)s\"").start();
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        String stdOut = null;
                        ArrayList<String> lastFileNames = new ArrayList<>();
                        while ((line = br.readLine()) != null) {
                            if (line.toLowerCase().contains("downloading playlist")) {
                                String[] result = line.substring(line.indexOf("playlist") + 10, line.length()).split("\n", 2);
                                if (new File("SouthPark " + currSeason + "." + currEpisode + " - " + result[0] + " (English).mkv").exists()) {
                                    lastFileNames.add("SouthPark " + currSeason + "." + currEpisode + " - " + result[0] + " (English).mkv");
                                    process.destroy();
                                    break;
                                }
                            }
                            toConsole = line;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (!toConsole.equals("")) {
                                        console.appendText(toConsole + "\n");
                                    }
                                }
                            });
                            stdOut = stdOut + line + "\n";
                            if (line.toLowerCase().contains("[download] finished downloading playlist:")) {
                                String[] result = stdOut.substring(stdOut.indexOf("playlist") + 10, stdOut.length()).split("\n", 2);
                                String temp = result[0];

                                Process process2 = null;
                                File[] dats = finder(new File(".").getCanonicalPath(), ".mp4");
                                if (temp.contains("&amp;")) {
                                    temp = temp.replaceAll("&amp;", "and");
                                }
                                if (temp.contains(":")) {
                                    temp = temp.replaceAll(":", "");
                                }
                                if (temp.contains("&")) {
                                    temp = temp.replaceAll("&", "and");
                                }
                                if (temp.contains("\"")) {
                                    temp = temp.replaceAll("\"", "");
                                }
                                try {
                                    String arg = "\"SouthPark " + currSeason + "." + currEpisode + " - " + temp + " (English).mkv\"";
                                    lastFileNames.add(arg);
                                    String arg2 = "";
                                    for (int i = 0; i < dats.length; i++) {
                                        if (dats.length - 1 == i) {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                        } else {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                            arg2 = arg2 + " + ";
                                        }
                                    }
                                    final URI uriMKVM;
                                    final URI exeMKVM;

                                    uriMKVM = getJarURI();
                                    exeMKVM = getFile(uriMKVM, "res/mkvmerge.exe");
                                    process2 = new ProcessBuilder(new File(exeMKVM).getAbsolutePath(), "-o", arg, arg2).start();
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }
                                InputStream is2 = process2.getInputStream();
                                InputStreamReader isr2 = new InputStreamReader(is2);
                                BufferedReader br2 = new BufferedReader(isr2);
                                String line2;

                                try {
                                    while ((line2 = br2.readLine()) != null) {
                                        System.out.println(line2);
                                        toConsole = line2;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!toConsole.equals("")) {
                                                    console.appendText(toConsole + "\n");
                                                }
                                            }
                                        });
                                    }
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }

                                for (File files : dats) {
                                    try {
                                        Files.delete(Paths.get(files.getAbsolutePath()));
                                    } catch (IOException ex) {
                                        StringWriter errors = new StringWriter();
                                        ex.printStackTrace(new PrintWriter(errors));
                                        JOptionPane.showMessageDialog(null, "" + errors.toString());
                                    }
                                }
                            }
                            System.out.println(line);
                        }
                        process = new ProcessBuilder(new File(exeYTDL).getAbsolutePath(), videoURLS.get(1), "--retries", "10", "--output", "\"" + workingDirectory.toString() + "\\temp%(title)s.%(ext)s\"").start();

                        is = process.getInputStream();
                        isr = new InputStreamReader(is);
                        br = new BufferedReader(isr);
                        line = null;
                        stdOut = null;


                        while ((line = br.readLine()) != null) {
                            final String test = line;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    console.appendText(test + "\n");
                                }
                            });
                            stdOut = stdOut + line + "\n";
                            if (line.toLowerCase().contains("[download] finished downloading playlist:")) {
                                String[] result = stdOut.substring(stdOut.indexOf("playlist") + 10, stdOut.length()).split("\n", 2);
                                String temp = result[0];
                                //lastFileNames.add(result[0]);

                                System.out.println(result[0]);

                                Process process2 = null;
                                File[] dats = finder(new File(".").getCanonicalPath(), ".mp4");
                                try {
                                    if (temp.contains("&amp;")) {
                                        temp = temp.replaceAll("&amp;", "und");
                                    }
                                    if (temp.contains(":")) {
                                        temp = temp.replaceAll(":", "");
                                    }
                                    if (temp.contains("&")) {
                                        temp = temp.replaceAll("&", "und");
                                    }
                                    if (temp.contains("\"")) {
                                        temp = temp.replaceAll("\"", "");
                                    }
                                    String arg = "\"SouthPark " + currSeason + "." + currEpisode + " - " + temp + " (German).mkv\"";
                                    lastFileNames.add(arg);
                                    String arg2 = "";
                                    for (int i = 0; i < dats.length; i++) {
                                        if (dats.length - 1 == i) {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                        } else {
                                            arg2 = arg2 + '"' + dats[i] + '"';
                                            arg2 = arg2 + " + ";
                                        }
                                    }
                                    final URI uriMKVM;
                                    final URI exeMKVM;

                                    uriMKVM = getJarURI();
                                    exeMKVM = getFile(uriMKVM, "res/mkvmerge.exe");
                                    process2 = new ProcessBuilder(new File(exeMKVM).getAbsolutePath(), "-o", arg, arg2).start();
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }
                                InputStream is2 = process2.getInputStream();
                                InputStreamReader isr2 = new InputStreamReader(is2);
                                BufferedReader br2 = new BufferedReader(isr2);
                                String line2;

                                try {
                                    while ((line2 = br2.readLine()) != null) {
                                        System.out.println(line2);
                                        toConsole = line2;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!toConsole.equals("")) {
                                                    console.appendText(toConsole + "\n");
                                                }
                                            }
                                        });
                                    }
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }

                                for (File files : dats) {
                                    try {
                                        Files.delete(Paths.get(files.getAbsolutePath()));
                                    } catch (IOException ex) {
                                        StringWriter errors = new StringWriter();
                                        ex.printStackTrace(new PrintWriter(errors));
                                        JOptionPane.showMessageDialog(null, "" + errors.toString());
                                    }
                                }

                                final URI uriMKVE;
                                final URI exeMKVE;

                                uriMKVE = getJarURI();
                                exeMKVE = getFile(uriMKVE, "res/mkvextract.exe");
                                String tempExtr = lastFileNames.get(1).replace(".mkv", ".ogg");
                                tempExtr = tempExtr.replaceAll(" ", ".");
                                lastFileNames.add(tempExtr);
                                process2 = new ProcessBuilder(new File(exeMKVE).getAbsolutePath(), "tracks", lastFileNames.get(1), "1:" + lastFileNames.get(2)).start();
                                is2 = process2.getInputStream();
                                isr2 = new InputStreamReader(is2);
                                br2 = new BufferedReader(isr2);

                                try {
                                    while ((line2 = br2.readLine()) != null) {
                                        System.out.println(line2);
                                        toConsole = line2;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!toConsole.equals("")) {
                                                    console.appendText(toConsole + "\n");
                                                }
                                            }
                                        });
                                    }
                                } catch (IOException ex) {
                                    StringWriter errors = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(errors));
                                    JOptionPane.showMessageDialog(null, "" + errors.toString());
                                }
                            }
                            System.out.println(line);
                        }


                        Process process2 = null;
                        try {
                            final URI uriMKVM;
                            final URI exeMKVM;

                            uriMKVM = getJarURI();
                            exeMKVM = getFile(uriMKVM, "res/mkvmerge.exe");
                            String fileName = lastFileNames.get(0).replace("(English)", "(" + languageStr + ")");
                            System.out.println(fileName);
                            process2 = new ProcessBuilder(new File(exeMKVM).getAbsolutePath(), "-o", fileName, "--language", "0:eng", lastFileNames.get(0), "--language", "0:de", lastFileNames.get(2)).start();

                        } catch (IOException ex) {
                            StringWriter errors = new StringWriter();
                            ex.printStackTrace(new PrintWriter(errors));
                            JOptionPane.showMessageDialog(null, "" + errors.toString());
                        }
                        InputStream is2 = process2.getInputStream();
                        InputStreamReader isr2 = new InputStreamReader(is2);
                        BufferedReader br2 = new BufferedReader(isr2);
                        String line2;

                        try {
                            while ((line2 = br2.readLine()) != null) {
                                System.out.println(line2);
                                toConsole = line2;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!toConsole.equals("")) {
                                            console.appendText(toConsole + "\n");
                                        }
                                    }
                                });
                            }
                        } catch (IOException ex) {
                            StringWriter errors = new StringWriter();
                            ex.printStackTrace(new PrintWriter(errors));
                            JOptionPane.showMessageDialog(null, "" + errors.toString());
                        }

                        Files.delete(Paths.get(new File(lastFileNames.get(2).replace("\"", "")).getAbsolutePath()));
                        Files.delete(Paths.get(new File(lastFileNames.get(1).replace("\"", "")).getAbsolutePath()));
                        Files.delete(Paths.get(new File(lastFileNames.get(0).replace("\"", "")).getAbsolutePath()));
                    }
                } catch (IOException ignored) {

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
            upperSplit.setDividerPositions(0.5);
            isDownloading = false;
        });
        t.start();
    }

    private static File[] finder(String dirName, String ending) {
        File dir = new File(dirName);

        return dir.listFiles((dir1, filename) -> filename.endsWith(ending) && filename.startsWith("temp"));

    }
}
