package com.xxworkshop.network.decoder;

import java.io.*;

/**
 * Created by brochexu on 8/27/14.
 */
public class TextDecoder implements Decoder {
    @Override
    public Object decode(InputStream inputStream) {
        String result = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while (null != (line = br.readLine())) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            result = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
