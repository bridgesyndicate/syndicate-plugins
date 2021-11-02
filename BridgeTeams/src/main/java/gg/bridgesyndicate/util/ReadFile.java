package gg.bridgesyndicate.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReadFile {
    private static String MINECRAFT_HOME = null;

    public static String pathToResources() {
        if (MINECRAFT_HOME == null) {
            if ( ( MINECRAFT_HOME = System.getenv("MINECRAFT_HOME") ) != null ){
                MINECRAFT_HOME = MINECRAFT_HOME + File.separator;
            } else {
                MINECRAFT_HOME = "./";
            }
        }
        return MINECRAFT_HOME;
    }

    public static String read(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        try( BufferedReader br =
                     new BufferedReader( new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            return sb.toString();
        }
    }
}
