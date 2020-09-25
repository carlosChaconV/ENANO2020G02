/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import fi.iki.elonen.NanoHTTPD;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;

import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import static fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT;
import static fi.iki.elonen.NanoHTTPD.getMimeTypeForFile;
import static fi.iki.elonen.NanoHTTPD.newChunkedResponse;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public final class Router extends RouterNanoHTTPD {

    private static final Pattern isFile = Pattern.compile("^.*[^/]$");
    private static final Integer port = 9000;

    public static void main(String[] args) {
        try {
            Router router = new Router();
        } catch (IOException ioe) {
            System.out.format("%nServer %s%n can't run", ioe.getLocalizedMessage());
        }
    }

    public Router() throws IOException {
        super(port);
        this.addMappings();
        this.start(SOCKET_READ_TIMEOUT, false);
        System.out.format("%nThe webserver is running on the port %d.\n", port);
        System.out.format("The url is http://localhost:%d/%n", port);
    }

    @Override
    public void addMappings() {
        this.addRoute("/class.*", CodeHandler.class);
        this.addRoute("/class", CodeHandler.class);
        this.addRoute("/inf.*", InfoHandler.class);
        this.addRoute("/inf", InfoHandler.class);
        this.addRoute("/(?!(class|info)).*", ResourceHandler.class);
    }

    public static class ResourceHandler extends RouterNanoHTTPD.StaticPageHandler {

        @Override
        public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            String uri = session.getUri();
            String file = (isFile.matcher(uri).matches() ? uri : uri + "index.html");
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream(file.substring(1));
            if (fileStream != null) {
                System.out.format("Successful response to '%s' static file request [%s].%n", file, session.getMethod());
                return newChunkedResponse(getStatus(), getMimeTypeForFile(file), fileStream);
            } else {
                System.out.format("Static file not found: '%s' [%s].%n", file, session.getMethod());
                return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT, "The requested resource does not exist.");
            }
        }
    }

    public static class InfoHandler extends RouterNanoHTTPD.DefaultStreamHandler {

        @Override
        public String getMimeType() {
            return MIME_PLAINTEXT;
        }

        @Override
        public IStatus getStatus() {
            return NanoHTTPD.Response.Status.OK;
        }

        @Override
        public InputStream getData() {
            return new ByteArrayInputStream("Your request was successful :D".getBytes());
        }

        public String InputStreamtoString(InputStream a) throws IOException {
            StringWriter writer = new StringWriter();
            IOUtils.copy(a, writer, "UTF-8");
            return writer.toString();
        }

        @Override
        public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            try {

                InputStream infoStream = getClass().getClassLoader().getResourceAsStream("src/inf.json");

                String info = InputStreamtoString(infoStream);

                System.out.format("Successful response to '%s' request [%s].%n", session.getUri(), session.getMethod());
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, MIME_PLAINTEXT, info);
            } catch (IOException e) {
                System.out.format("Invalid request for '%s' [%s].%n", session.getUri(), session.getMethod());
                return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT, "The requested resource does not exist.");
            }
        }
    }

    public static class CodeHandler extends RouterNanoHTTPD.DefaultStreamHandler {

        @Override
        public String getMimeType() {
            return MIME_PLAINTEXT;
        }

        @Override
        public IStatus getStatus() {
            return NanoHTTPD.Response.Status.OK;
        }

        @Override
        public InputStream getData() {
            return new ByteArrayInputStream("Your request was successful :D".getBytes());
        }

        @Override
        public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            try {
                Map<String, String> response = new HashMap<>();
                session.parseBody(response);

                String postData = response.get("postData");

                JSONObject jsonData = new JSONObject(postData);
                String data = jsonData.getString("data");

                System.out.format("Successful response to '%s' request [%s].", session.getUri(), session.getMethod());
                System.out.format("The data is: '%s'.%n", data);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, MIME_PLAINTEXT, "The request was successful.\nYour data is: '" + data + "'");
            } catch (IOException | NanoHTTPD.ResponseException e) {
                System.out.format("Invalid request for '%s' [%s].%n", session.getUri(), session.getMethod());
                return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT, "The requested resource does not exist.");
            }
        }
    }
}

