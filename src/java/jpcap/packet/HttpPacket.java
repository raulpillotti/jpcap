package jpcap.packet;


import jpcap.utils.HttpFieldsHelper;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HttpPacket extends TCPPacket {
    private String data;

    private String firstLine = "";
    private String userAgent = "";
    private String host = "";
    private String accept = "";
    private String acceptLanguage = "";
    private String acceptEncoding = "";
    private String contentLength = "";
    private String connection = "";
    private String contentType = "";
    private String date = "";
    private String from = "";
    private String lastModified = "";
    private String server = "";
    private String acceptRanges = "";
    private String cacheControl = "";
    private String referer = "";
    private String cookie = "";
    private String authorization = "";
    private String proxyAuthorization = "";

    private boolean response = false;

    public HttpPacket(TCPPacket p) {
        super(p.src_port, p.dst_port, p.sequence, p.ack_num, p.urg, p.ack, p.psh, p.rst, p.syn, p.fin, p.rsv1, p.rsv2, p.window, 0);
        this.data = new String(p.data, StandardCharsets.UTF_8);
        this.src_ip = p.src_ip;
        this.dst_ip = p.dst_ip;
        this.sequence = p.sequence;
        this.rsv1 = p.rsv1;
        this.rsv2 = p.rsv2;
        this.window = p.window;
        this.datalink = p.datalink;
        configureHTTPHeader();
    }

    public void configureHTTPHeader() {
        HashMap<String, String> fieldContent = new HashMap<>();

        String data = this.data;
        String[] lines = data.split("\r\n");
        List<String> linesList = Arrays.asList(lines);
        if (lines.length > 0) {
            this.response = isHttpResponse(lines[0]);

            for (String line : linesList) {
                if (linesList.indexOf(line) == 0) {
                    firstLine = line;
                    continue;
                }

                String[] linesParts = line.split(":");

                if (linesParts.length > 1) {
                    String field = linesParts[0];
                    List<String> partsList = new ArrayList<String>(Arrays.asList(linesParts));
                    partsList.remove(0);
                    String content = String.join("", partsList);
                    fieldContent.put(field, content);
                } else {
                    fieldContent.put("content", line);
                }
            }
        }

        if (!fieldContent.isEmpty()) {
            for (String key : fieldContent.keySet()) {
                String value = fieldContent.get(key);
                if (key.equals("content")) {
                    if (!value.isEmpty()) {
                        continue;
                    }
                }
                setHttpField(key, value);
            }
        }

    }

    private void setHttpField(String key, String value) {
        HttpFieldsHelper helper = new HttpFieldsHelper();
        String field = helper.field(key);

        if (field != null) {
            try {
                if (key != "content") {
                    Field declaredField = HttpPacket.class.getDeclaredField(field);
                    declaredField.set(this, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static boolean isHttpResponse(String line) {
        String[] parts = line.split(" ");

        List<String> lengthThreeStr = Arrays.stream(parts).filter(str -> str.length() == 3)
                .toList();
        for (String code : lengthThreeStr) {
            try {
                Integer.parseInt(code);
                return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    public static boolean isHttp(byte[] byteData) {
        String data = new String(byteData, StandardCharsets.UTF_8);
        if (data.startsWith("GET ") || data.startsWith("POST ")
                || data.startsWith("HTTP/1.") || data.contains("HTTP") || data.contains("http"))
            return true;

        return false;
    }

    @Override
    public String toString() {
        String str = "Pacote HTTP " + (response ? "(response)" : "(request)") + "\n";
        str += " Head = " + firstLine + "\n";
        str += " Endereço de origem = " + src_ip + "\n";
        str += " Endereço de destino = " + dst_ip + "\n";
        str += " Porta de origem = " + src_port + "\n";
        str += " Porta de destino = " + dst_port + "\n";
        str += (!userAgent.isEmpty() ? (" User agent = " + userAgent + "\n") : "");
        str += (!host.isEmpty() ? (" Host = " + host + "\n") : "");
        str += (!accept.isEmpty() ? (" Accept = " + accept + "\n") : "");
        str += (!acceptLanguage.isEmpty() ? (" AcceptLanguage = " + acceptLanguage + "\n") : "");
        str += (!acceptEncoding.isEmpty() ? (" AcceptEncoding = " + acceptEncoding + "\n") : "");
        str += (!contentLength.isEmpty() ? (" ContentLength = " + contentLength + "\n") : "");
        str += (!connection.isEmpty() ? (" Connection = " + connection + "\n") : "");
        str += (!contentType.isEmpty() ? (" ContentType = " + contentType + "\n") : "");
        str += (!date.isEmpty() ? (" Date = " + date + "\n") : "");
        str += (!from.isEmpty() ? (" From = " + from + "\n") : "");
        str += (!lastModified.isEmpty() ? (" LastModified = " + lastModified + "\n") : "");
        str += (!server.isEmpty() ? (" Server = " + server + "\n") : "");
        str += (!acceptRanges.isEmpty() ? (" AcceptRanges = " + acceptRanges + "\n") : "");
        str += (!cacheControl.isEmpty() ? (" CacheControl = " + cacheControl + "\n") : "");
        str += (!referer.isEmpty() ? (" Referer = " + referer + "\n") : "");
        str += (!cookie.isEmpty() ? (" Cookie = " + cookie + "\n") : "");
        str += (!authorization.isEmpty() ? (" Authorization = " + authorization + "\n") : "");
        str += (!proxyAuthorization.isEmpty() ? (" ProxyAuthorization = " + proxyAuthorization + "\n") : "");
        return str;
    }
}
