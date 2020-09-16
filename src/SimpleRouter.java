package cr.una.paradigmas.enano;

import java.io.IOException;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import static fi.iki.elonen.NanoHTTPD.Response;

import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT;

import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.IndexHandler;


public class SimpleRouter extends RouterNanoHTTPD {
    static int PORT = 8080;
    static record Code(String source){
        public String toString(){
               return String.format("{\"code\":\"%s\"}", source);
        }
    }
    static public class CodeHandler extends DefaultHandler {
        List<Code> code = Arrays.asList(new Code("class A{}"), new Code("class B{}"));
        @Override
        public String getText() {
            return code.stream()
                       .map(Code::toString)
                       .collect(Collectors.joining(",", "[", "]"));
            
        }
 
        @Override
        public String getMimeType() {
            //return MIME_PLAINTEXT;
            return "application/json";
        }
     
        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }
    }
    
    public SimpleRouter(int port) throws IOException {
        super(port);
        addMappings();
        start(SOCKET_READ_TIMEOUT, false);
        System.out.format("*** Router running on port %d ***%n", port);
    }
 
    @Override
    public void addMappings() {
        addRoute("/", IndexHandler.class);
        addRoute("/codes", CodeHandler.class);
    }
    
    public static void main(String[] args ) throws IOException {
        PORT = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        new SimpleRouter(PORT);
    }
}