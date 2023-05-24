///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS com.google.code.gson:gson:2.10.1
import static java.lang.System.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class versions {

    public static void main(String... args) {
        String servers = readServersJson();
        JsonObject serversGson = new Gson().fromJson(servers, JsonObject.class);
        List<String> fullVersionsInDescriptor = findAllFullVersions(serversGson);
        Map<String, String> mmtlm = majorMinorToLatestMicro(fullVersionsInDescriptor);
        List<String> toAddRaw = findVersionsToAdd(fullVersionsInDescriptor, mmtlm);
        List<String> toAddFiltered = new ArrayList<String>(toAddRaw).stream().filter(c -> {
            if( c.contains("-M")) {
                String prefix = c.substring(0, c.indexOf("-M"));
                if( fullVersionsInDescriptor.contains(prefix) || toAddRaw.contains(prefix)) {
                    return false;
                }
            }
            return true;
        }).toList();
        List<String> toAdd = new ArrayList<String>(toAddFiltered);
        toAdd.sort(getComparator());
        for( String singleAddition : toAdd ) {
            ensureVersionAdded(singleAddition, serversGson);
        }

        System.out.println("\n\n\n");

        // Remove unneeded milestones
        List<String> fullVersionsInDescriptorNew = findAllFullVersions(serversGson);
        for( String oneVersion : fullVersionsInDescriptorNew ) {
            if( oneVersion.contains("-M")) {
                String majorMinorMicro = oneVersion.substring(0, oneVersion.indexOf("-M"));
                if( fullVersionsInDescriptorNew.contains(majorMinorMicro)) {
                    // the x.y.0 (no milestone) release exists, don't add the milestones
                    removeDownloadVersion(oneVersion, serversGson);
                } else {
                    List<String> milestones = fullVersionsInDescriptorNew.stream().filter(c -> {
                        return c.startsWith(majorMinorMicro + "-M");
                    }).collect(Collectors.toList());
                    milestones.sort(getComparator());
                    if( milestones.indexOf(oneVersion) != 0) {
                        removeDownloadVersion(oneVersion, serversGson);
                    }
                }
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(serversGson);   
        //System.out.println("\n\n\nNew file contents: " + jsonOutput);
        writeToFile(jsonOutput);
    }

    private static void removeDownloadVersion(String string, JsonObject serversGson) {
        System.out.println("Removing outdated milestone " + string);
        JsonObject serverTypes = (JsonObject)serversGson.get("serverTypes");
        Set<String> serverTypeSet = serverTypes.keySet();
        for(String st : serverTypeSet){
            JsonObject oneType = (JsonObject)serverTypes.get(st);
            if( oneType != null ) {
                JsonObject downloads = (JsonObject)oneType.get("downloads");
                if( downloads != null ) {
                    List<String> dlVersions = new ArrayList<String>(downloads.keySet());
                    for( String dlVers : dlVersions) {
                        if( !dlVers.equals("downloadProviderId")) {
                            JsonObject oneVersionObject = (JsonObject)downloads.get(dlVers);
                            if( oneVersionObject != null ) {
                                String fullVers = oneVersionObject.getAsJsonPrimitive("fullVersion").getAsString();
                                if( string.equals(fullVers)) {
                                    downloads.remove(dlVers);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private static void ensureVersionAdded(String singleAddition, JsonObject serversGson) {
        JsonObject serverTypes = (JsonObject)serversGson.get("serverTypes");
        String serverTypeKey = "org.jboss.ide.eclipse.as.server.tomcat." + majorMinor(singleAddition);
        JsonObject typeObj = (JsonObject)serverTypes.get(serverTypeKey);
        if( typeObj == null ) {
            System.out.println("Adding server type " + serverTypeKey);
            JsonObject generatedType = new JsonObject();
            generatedType.addProperty("template", "tomcat.template");
            JsonObject generatedDiscoveries = new JsonObject();
            String majorDiscoveryKey = "tomcat." + major(singleAddition) + ".x";
            JsonObject majorDiscoveryVal = new JsonObject();
            majorDiscoveryVal.addProperty("discoveryType", "jarManifest");
            majorDiscoveryVal.addProperty("name", "Tomcat " + major(singleAddition) + ".x");
            majorDiscoveryVal.addProperty("nameFile", "lib/catalina.jar");
            majorDiscoveryVal.addProperty("nameKey", "Implementation-Title");
            majorDiscoveryVal.addProperty("nameRequiredPrefix", "Apache Tomcat");
            majorDiscoveryVal.addProperty("versionFile", "lib/catalina.jar");
            majorDiscoveryVal.addProperty("versionKey", "Implementation-Version");
            majorDiscoveryVal.addProperty("versionRequiredPrefix", major(singleAddition) + ".");
            generatedDiscoveries.add(majorDiscoveryKey, majorDiscoveryVal);
            generatedType.add("discoveries", generatedDiscoveries);

            JsonObject generatedDownloads = new JsonObject();
            generatedDownloads.addProperty("downloadProviderId", "tomcat" + major(singleAddition) + ".0.x");
            generatedType.add("downloads", generatedDownloads);

            JsonObject generatedInnerType = new JsonObject();
            generatedInnerType.addProperty("name", "Tomcat " + major(singleAddition) + ".x");
            String descString = "A server adapter capable of discovering and controlling a Tomcat " + major(singleAddition) + ".x runtime instance.";
            generatedInnerType.addProperty("description", descString);

            generatedInnerType.addProperty("description", "Tomcat " + major(singleAddition) + ".x");
            generatedType.add("type", generatedInnerType);

            serverTypes.add(serverTypeKey, generatedType);
            typeObj = generatedType;
        }
        JsonObject downloadsForType = (JsonObject) typeObj.get("downloads");
        String dlKey = "tomcat-" + singleAddition;
        if( downloadsForType.get(dlKey) == null ) {
            System.out.println("Adding downloadable version " + dlKey);
            JsonObject generatedDownload = new JsonObject();
            int size = discoverSize(singleAddition);
            if( size > -1 ) {
                String dlUrl = "https://archive.apache.org/dist/tomcat/tomcat-" + major(singleAddition) + "/v" + singleAddition + "/bin/apache-tomcat-" + singleAddition + ".zip";
                generatedDownload.addProperty("name", "Apache Tomcat " + singleAddition);
                generatedDownload.addProperty("fullVersion", singleAddition);
                generatedDownload.addProperty("downloadUrl", dlUrl);
                generatedDownload.addProperty("licenseUrl", "https://www.apache.org/licenses/LICENSE-2.0.txt");
                generatedDownload.addProperty("installationMethod", "archive");
                generatedDownload.addProperty("size", "" + size);
                downloadsForType.add(dlKey, generatedDownload);
            }
        }
    }

    public static int discoverSize(String fullVersion) {
        try {
            String dlUrl = "https://archive.apache.org/dist/tomcat/tomcat-" + major(fullVersion) + "/v" + fullVersion + "/bin/apache-tomcat-" + fullVersion + ".zip";
            URL url = new URL(dlUrl);
            return getFileSize(url);
        } catch( Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }
    public static List<String> findVersionsToAdd(List<String> fullVersionsInDescriptor, Map<String, String> mmtlm) {
        List<String> toAdd = new ArrayList<String>();
        Integer[] versions = readMajors();
        for( int i = 0; i < versions.length; i++ ) {
            String[] full = readFullVersions(versions[i].intValue());
            for( int j = 0; j < full.length; j++ ) {
                boolean missing = !fullVersionsInDescriptor.contains(full[j]);
                boolean newer = newerThanDescriptorLatest(full[j], mmtlm); 
                boolean needToAdd = missing && newer;
                if( needToAdd ) {
                    toAdd.add(full[j]);
                }
                //System.out.println("   major " + versions[i] + ": " + full[j] + ", missing=" + missing + ", newer=" + newer + ", needToAdd=" + needToAdd);
            }
        }
        return toAdd;
    }

    private static boolean newerThanDescriptorLatest(String v, Map<String,String> majorMinorToLatestMicro) {
        String[] split = v.split("\\.");
        String majorDotMinor = split[0] + "." + split[1];
        String latest = majorMinorToLatestMicro.get(majorDotMinor);
        if( latest == null )
            return true;
        String v2 = majorDotMinor + "." + latest;
        return compareVersions(v, v2) > 1;
    }

    private static int compareVersions(String v1, String v2) {
        return getComparator().compare(v1, v2);
    }
    private static Comparator<String> getComparator() {
        return new Comparator<String>() {
            public int compare(String o1, String o2) {
                String[] majorMinorMicro1 = o1.split("\\.");
                String[] majorMinorMicro2 = o2.split("\\.");
                int major1 = Integer.parseInt(majorMinorMicro1[0]);
                int major2 = Integer.parseInt(majorMinorMicro2[0]);
                if( major1 != major2 ) {
                    return major2 - major1;
                }
                int minor1 = Integer.parseInt(majorMinorMicro1[1]);
                int minor2 = Integer.parseInt(majorMinorMicro2[1]);
                if( minor1 != minor2 ) {
                    return minor2 - minor1;
                }
                String micro1S = (majorMinorMicro1[2].indexOf("-M") == -1 ? majorMinorMicro1[2] : majorMinorMicro1[2].substring(0, majorMinorMicro1[2].indexOf("-M")));
                String micro2S = (majorMinorMicro2[2].indexOf("-M") == -1 ? majorMinorMicro2[2] : majorMinorMicro2[2].substring(0, majorMinorMicro2[2].indexOf("-M")));
                int micro1 = Integer.parseInt(micro1S);
                int micro2 = Integer.parseInt(micro2S);
                if( micro1 != micro2 ) {
                    return micro2 - micro1;
                }
                if( majorMinorMicro1[2].indexOf("-M") == -1 && majorMinorMicro2[2].indexOf("-M") == -1 ) {
                    return 0;
                }
                if( majorMinorMicro1[2].indexOf("-M") > -1 && majorMinorMicro2[2].indexOf("-M") == -1 ) {
                    return 1;
                }
                if( majorMinorMicro2[2].indexOf("-M") > -1 && majorMinorMicro1[2].indexOf("-M") == -1 ) {
                    return -1;
                }
                String milestone1 = majorMinorMicro1[2].substring(majorMinorMicro1[2].indexOf("-M") + 2);
                String milestone2 = majorMinorMicro2[2].substring(majorMinorMicro2[2].indexOf("-M") + 2);
                int m1 = Integer.parseInt(milestone1);
                int m2 = Integer.parseInt(milestone2);
                return m2 - m1;
            }
        };
    }
    private static Map<String, String> majorMinorToLatestMicro(List<String> source) {
        List<String> sorted = new ArrayList<String>(source);
        sorted.sort(getComparator());
        Collections.reverse(sorted);
        Map<String,String> ret = new HashMap<String, String>();
        for( String ver : sorted) {
            String[] segments = ver.split("\\.");
            String k = segments[0] + "." + segments[1];
            String v = segments[2];
            String fromMap = ret.get(k);
            if( fromMap == null || fromMap.compareTo(v) < 0) {
                ret.put(k,v);
            }
        }
        return ret;
    }

    private static String major(String v) {
        String[] segments = v.split("\\.");
        String k = segments[0];
        return k;
    }
    private static String majorDotMinor(String v) {
        String[] segments = v.split("\\.");
        String k = segments[0] + "." + segments[1];
        return k;
    }
    private static String majorMinor(String v) {
        String[] segments = v.split("\\.");
        String k = segments[0] + segments[1];
        return k;
    }

    private static List<String> findAllFullVersions(JsonObject serversGson) {
        ArrayList<String> collector = new ArrayList<>();
        JsonObject serverTypes = (JsonObject)serversGson.get("serverTypes");
        Set<String> serverTypeSet = serverTypes.keySet();
        for(String st : serverTypeSet){
            JsonObject oneType = (JsonObject)serverTypes.get(st);
            if( oneType != null ) {
                JsonObject downloads = (JsonObject)oneType.get("downloads");
                if( downloads != null ) {
                    Set<String> dlVersions = downloads.keySet();
                    for( String dlVers : dlVersions) {
                        if( !dlVers.equals("downloadProviderId")) {
                            JsonObject oneVersionObject = (JsonObject)downloads.get(dlVers);
                            if( oneVersionObject != null ) {
                                String fullVers = oneVersionObject.getAsJsonPrimitive("fullVersion").getAsString();
                                collector.add(fullVers);
                            }
                        }
                    }
                }
            }
        }
        return collector;
    }

    public static Integer[] readMajors() {
        ArrayList<Integer> collector = new ArrayList<Integer>();
        try {
            URL oracle = new URL("https://archive.apache.org/dist/tomcat/");
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if( inputLine.contains("folder.gif")) {
                    int ind = inputLine.indexOf("<a href=");
                    if( ind > -1 ) {
                        String sub = inputLine.substring(ind+9);
                        int end = sub.indexOf("\"");
                        if( end > -1 ) {
                            String sub2 = sub.substring(0,end);
                            if( sub2.startsWith("tomcat-")) {
                                String sub3 = sub2.substring(7);
                                if( sub3.endsWith("/")) {
                                    sub3 = sub3.substring(0, sub3.length() - 1);
                                }
                                try {
                                    int v = Integer.parseInt(sub3);
                                    if( v > 9 ) {
                                        collector.add(v);
                                    }
                                } catch( NumberFormatException nfe) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }
            in.close();
        } catch(Throwable t) {
            t.printStackTrace();
        } finally { 
        }
        return collector.stream().toArray(Integer[] ::new);
    }


    public static String[] readFullVersions(int major) {
        String url = "https://archive.apache.org/dist/tomcat/tomcat-" + major;
        ArrayList<String> collector = new ArrayList<String>();
        try {
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if( inputLine.contains("folder.gif")) {
                    int ind = inputLine.indexOf("<a href=");
                    if( ind > -1 ) {
                        String sub = inputLine.substring(ind+9);
                        int end = sub.indexOf("\"");
                        if( end > -1 ) {
                            String sub2 = sub.substring(0,end);
                            if( sub2.startsWith("v")) {
                                String sub3 = sub2.substring(1);
                                if( sub3.endsWith("/")) {
                                    sub3 = sub3.substring(0, sub3.length() - 1);
                                    collector.add(sub3);
                                }
                            }
                        }
                    }
                }
            }
            in.close();
        } catch(Throwable t) {
            t.printStackTrace();
        } finally { 
        }
        return collector.stream().toArray(String[] ::new);
    }

    public static String readServersJson() {
        String file = "../src/main/resources/servers.json";
        try {
            File f = new File(file);
            System.out.println("Writing contents to " + f.toPath());
            String s = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining("\n"));
            return s;
        } catch(Throwable t) {
            t.printStackTrace();
        } finally { 
        }
        return null;
    }

    public static void writeToFile(String contents) {
        String file = "../src/main/resources/servers.json";
        try {
            File f = new File(file);
            Files.writeString(f.toPath(), contents);
        } catch(Throwable t) {
            t.printStackTrace();
        } finally { 
        }
    }


    private static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }
    }
}
