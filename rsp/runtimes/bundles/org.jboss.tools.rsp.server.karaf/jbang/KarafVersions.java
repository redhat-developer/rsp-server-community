///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS com.google.code.gson:gson:2.10.1

import java.io.BufferedReader;
import java.io.File;
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

public class KarafVersions {

    public static void main(String... args) {
        String servers = readServersJson();
        JsonObject serversGson = new Gson().fromJson(servers, JsonObject.class);
        List<String> fullVersionsInDescriptor = findAllFullVersions(serversGson);
        Map<String, String> mmtlm = majorMinorToLatestMicro(fullVersionsInDescriptor);
        List<String> toAddRaw = findVersionsToAdd(fullVersionsInDescriptor, mmtlm);
        List<String> toAdd = new ArrayList<String>(toAddRaw);
        toAdd.sort(getComparator());
        for( String singleAddition : toAdd ) {
            ensureVersionAdded(singleAddition, serversGson);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(serversGson);   
        // //System.out.println("\n\n\nNew file contents: " + jsonOutput);
        writeToFile(jsonOutput);
    }

    private static void ensureVersionAdded(String singleAddition, JsonObject serversGson) {
        JsonObject serverTypes = (JsonObject)serversGson.get("serverTypes");
        String serverTypeKey = "org.jboss.ide.eclipse.as.server.karaf." + majorMinor(singleAddition);
        JsonObject typeObj = (JsonObject)serverTypes.get(serverTypeKey);
        if( typeObj == null ) {
            System.out.println("Adding server type " + serverTypeKey);
            JsonObject generatedType = new JsonObject();
            generatedType.addProperty("template", "karaf.template");
            JsonObject generatedDiscoveries = new JsonObject();
            String majorDiscoveryKey = "apache.karaf." + majorMinor(singleAddition);
            JsonObject majorDiscoveryVal = new JsonObject();
            majorDiscoveryVal.addProperty("discoveryType", "jarManifest");
            majorDiscoveryVal.addProperty("name", "Apache Karaf " + majorDotMinor(singleAddition));
            majorDiscoveryVal.addProperty("nameFile", "lib/boot/org.apache.karaf.main-*.jar");
            majorDiscoveryVal.addProperty("nameKey", "Bundle-Name");
            majorDiscoveryVal.addProperty("nameRequiredPrefix", "Apache Karaf");
            majorDiscoveryVal.addProperty("versionFile", "lib/boot/org.apache.karaf.main-*.jar");
            majorDiscoveryVal.addProperty("versionKey", "Bundle-Version");
            majorDiscoveryVal.addProperty("versionRequiredPrefix", majorDotMinor(singleAddition) + ".");
            generatedDiscoveries.add(majorDiscoveryKey, majorDiscoveryVal);
            generatedType.add("discoveries", generatedDiscoveries);

            JsonObject generatedDownloads = new JsonObject();
            generatedDownloads.addProperty("downloadProviderId", "karaf" + majorMinor(singleAddition) + ".download");
            generatedType.add("downloads", generatedDownloads);

            JsonObject generatedInnerType = new JsonObject();
            generatedInnerType.addProperty("name", "Apache Karaf " + majorDotMinor(singleAddition));
            String descString = "A server adapter capable of discovering and controlling an Apache Karaf " + majorDotMinor(singleAddition) + " container.";
            generatedInnerType.addProperty("description", descString);
            generatedType.add("type", generatedInnerType);

            serverTypes.add(serverTypeKey, generatedType);
            typeObj = generatedType;
        }
        JsonObject downloadsForType = (JsonObject) typeObj.get("downloads");
        String dlKey = "karaf-" + singleAddition;
        if( downloadsForType.get(dlKey) == null ) {
            System.out.println("Adding downloadable version " + dlKey);
            JsonObject generatedDownload = new JsonObject();
            int size = discoverSize(singleAddition);
            if( size > -1 ) {
                String dlUrl = getDownloadUrl(singleAddition);
                generatedDownload.addProperty("name", "Apache Karaf " + singleAddition);
                generatedDownload.addProperty("fullVersion", singleAddition);
                generatedDownload.addProperty("downloadUrl", dlUrl);
                generatedDownload.addProperty("licenseUrl", "https://www.apache.org/licenses/LICENSE-2.0.txt");
                generatedDownload.addProperty("installationMethod", "archive");
                generatedDownload.addProperty("size", "" + size);
                downloadsForType.add(dlKey, generatedDownload);
            }
        }
    }

    public static String getDownloadUrl(String fullVersion) {
        return "https://archive.apache.org/dist/karaf/" + fullVersion + "/apache-karaf-" + fullVersion + ".zip";
    }

    public static int discoverSize(String fullVersion) {
        try {
            String dlUrl = getDownloadUrl(fullVersion);
            URL url = new URL(dlUrl);
            return getFileSize(url);
        } catch( Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }
    public static List<String> findVersionsToAdd(List<String> fullVersionsInDescriptor, Map<String, String> mmtlm) {
        ArrayList<String> toAdd = new ArrayList<String>();
        String[] versions = findAllVersionsFromWebArchive();
        for( int i = 0; i < versions.length; i++ ) {
            String[] split = versions[i].split("\\.");
            if( Integer.parseInt(split[0])> 5 ) {
                toAdd.add(versions[i]);
            }
        }
        return toAdd;
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
    private static String majorMinor(String v) {
        String[] segments = v.split("\\.");
        String k = segments[0] + segments[1];
        return k;
    }

    private static String majorDotMinor(String v) {
        String[] segments = v.split("\\.");
        String k = segments[0] + "." + segments[1];
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

    public static String[] findAllVersionsFromWebArchive() {
        ArrayList<String> collector = new ArrayList<String>();
        try {
            URL oracle = new URL("https://archive.apache.org/dist/karaf/");
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                int ind = inputLine.indexOf("<a href=");
                if( ind > -1 ) {
                    String sub = inputLine.substring(ind+9);
                    int end = sub.indexOf("\"");
                    if( end > -1 ) {
                        String quotedFolder = sub.substring(0,end - 1);
                        String[] segments = quotedFolder == null ? new String[0] : quotedFolder.split("\\.");
                        if( segments.length == 3 ) {
                            String major = segments[0];
                            String minor = segments[1];
                            if( major.matches("-?\\d+") && minor.matches("-?\\d+")) {
                                // 4.3 minimum
                                if( Integer.parseInt(major) > 4 || Integer.parseInt(major) == 4 && Integer.parseInt(minor) >= 3) {
                                    collector.add(quotedFolder);
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
