import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * a samplify of curl app to send a request with java tools
 */
public class MURL {

    public MURL() {

    }

    /**
     * send a data to server
     * 
     * @param map                  map of data
     * @param boundary
     * @param bufferedOutputStream
     * @throws IOException
     */
    public static void bufferOutFormData(HashMap<String, String> map, String boundary,
            BufferedOutputStream bufferedOutputStream) throws IOException {
        for (String key : map.keySet()) {
            bufferedOutputStream.write(("--" + boundary + "\r\n").getBytes());
            if (key.contains("file")) {
                bufferedOutputStream.write(("Content-Disposition: form-data; filename=\""
                        + (new File(map.get(key))).getName() + "\"\r\nContent-Type: Auto\r\n\r\n").getBytes());
                try {
                    BufferedInputStream tempBufferedInputStream = new BufferedInputStream(
                            new FileInputStream(new File(map.get(key))));
                    int nRead = -1;
                    byte[] data1 = new byte[1024];
                    while ((nRead = tempBufferedInputStream.read(data1)) != -1) {
                        bufferedOutputStream.write(data1, 0, nRead);
                    }
                    bufferedOutputStream.write("\r\n".getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                bufferedOutputStream.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
                bufferedOutputStream.write((map.get(key) + "\r\n").getBytes());
            }
        }
        bufferedOutputStream.write(("--" + boundary + "--\r\n").getBytes());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    /**
     * manage program
     * 
     * @param args inut args of console
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void run(String[] args) throws MalformedURLException, IOException {

        ArrayList<HTTPRequest> requestsList = new ArrayList<>();
        File requestsFile = new File("requestsFile.txt");
        File saveResponse = null;
        HttpURLConnection con = null;
        HTTPRequest request = new HTTPRequest();
        String programName = "murl";
        // Scanner scan = new Scanner(System.in);
        // String args[i] = null;
        boolean showResponseHeaders = false;
        boolean saveOutput = false;
        boolean saveRequest = false;
        URL url = null;
        String urlHost;
        String method = "GET";
        String[] headers = null;
        String[] data = null;
        boolean existHeader = false;
        boolean existBody = false;
        String path;
        boolean followRedirect = false;

        try {
            FileInputStream fileIn = new FileInputStream(requestsFile);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Object obj = objectIn.readObject();
            requestsList = (ArrayList<HTTPRequest>) obj;

        } catch (EOFException e) {
            // System.out.println("End of file reached");
        } catch (ClassNotFoundException e) {
            // System.out.println("Class not found");
        }
        int status = 0;
        showResponseHeaders = false;
        saveOutput = false;
        saveRequest = false;
        method = "GET";
        headers = null;
        data = null;
        existHeader = false;
        existBody = false;

        if (args[0].equals("--help") || args[0].equals("-h")) {
            System.out.println(
                    "-M or --m to set method\n-H or --headers to set request headers\n-i to show or hide response headers\n-O or --output to save response body in a file\n-S or --save to save request -d or --data to set message body\nlist to show list of saved requests");
            return;
        } else if (args[0].equals("list")) {
            try {

                int i = 0;

                for (HTTPRequest requ : requestsList) {
                    i++;
                    System.out.println(i + "- url: " + requ.getUrl().getHost() + " | method: " + requ.getMethod()
                            + " | headers: " + requ.headersString());
                }

                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (args[0].equals("fire")) {

            for (int j = 1; j < args.length; j++) {
                int num = -1;
                HTTPRequest requ = new HTTPRequest();
                try {
                    num = Integer.parseInt(args[j]);
                } catch (NumberFormatException e) {
                    System.out.println(args[j] + " is'nt numeric");
                }
                try {
                    requ = requestsList.get(num - 1);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(args[j] + " is out of bound");
                }

                doRequest(requ);

            }

        } else {

            urlHost = args[0];
            try {
                url = new URL(urlHost);
                request.setUrl(url);
                con = (HttpURLConnection) url.openConnection();
                // status = con.getResponseCode();
                // System.out.println(status);
                // con.setRequestMethod(method);
                // request.setMethod(method);
            } catch (Exception e) {
                System.out.println("invalid url");
            }

        }
        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-i")) {
                showResponseHeaders = true;
            }

            else if (args[i].equals("-O") || args[i].equals("--output")) {

                path = args[i + 1];
                saveResponse = new File(args[i]);
                saveOutput = true;

            }

            else if (args[i].equals("-f")) {
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER) {

                        String newUrl = con.getHeaderField("Location");
                        System.out.println(newUrl);
                        url = new URL(newUrl);
                        // open the new connnection again
                        con = (HttpURLConnection) url.openConnection();
                        request.setUrl(url);
                    }
                }
            }

            else if (args[i].equals("-M") || args[i].equals("--method")) {

                if (args[i + 1].equals("GET") || args[i + 1].equals("POST") || args[i + 1].equals("DELETE")
                        || args[i + 1].equals("PUT")) {
                    method = args[i + 1];
                }
                con.setRequestMethod(method);
                request.setMethod(method);
            }

            else if (args[i].equals("-H") || args[i].equals("--headers")) {

                headers = args[i + 1].split(";");
                existHeader = true;

            }

            else if ((args[i].equals("-d") || args[i].equals("--data"))) {

                data = args[i + 1].split("&");
                existBody = true;

            }

            else if ((args[i].equals("-S") || args[i].equals("--save"))) {
                saveRequest = true;
            }
        }
        if (existHeader) {
            for (int j = 0; j < headers.length; j++) {
                System.out.println(headers[j]);
                String[] pair = headers[j].split(":");
                request.addHeader(pair[0], pair[1]);
            }
        }
        if (existBody) {
            for (int j = 0; j < data.length; j++) {

                String[] pair = data[j].split("=");
                request.addBody(pair[0], pair[1]);
            }
        }

        if (saveRequest) {

            try {

                requestsList.add(request);
                FileOutputStream outStream = new FileOutputStream(requestsFile);
                ObjectOutputStream out = new ObjectOutputStream(outStream);
                out.writeObject(requestsList);
                out.flush();
                outStream.flush();
                out.close();
                outStream.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println(method);
        try {

            if (!method.equals("GET") && !method.equals("DELETE")) {
                con.setDoOutput(true);
            }
            for (int j = 0; j < request.getHeaders().size(); j++) {
                String[] pair = headers[j].split(":");
                con.setRequestProperty(pair[0], pair[1]);
            }

            if (method.equals("GET")) {

                System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                if (showResponseHeaders) {
                    System.out.println("headers: " + con.getHeaderFields());
                }
                System.out.println("Response Body:");
                if (saveOutput) {

                    InputStream inputStream = con.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(saveResponse);
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    outputStream.close();
                    inputStream.close();

                } else {
                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();
                }

            } else if (method.equals("POST")) {
                try {
                    String boundary = System.currentTimeMillis() + "";
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    BufferedOutputStream request1 = new BufferedOutputStream(con.getOutputStream());
                    bufferOutFormData(request.getBody(), boundary, request1);
                    System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                    if (showResponseHeaders) {
                        System.out.println("headers: " + con.getHeaderFields());

                    }
                    System.out.println("Response Body:");
                    if (saveOutput) {

                        InputStream inputStream = con.getInputStream();
                        FileOutputStream outputStream = new FileOutputStream(saveResponse);
                        int bytesRead = -1;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            System.out.println(new String(buffer, 0, bytesRead));
                        }
                        outputStream.close();
                        inputStream.close();

                    } else {
                        InputStream inputStream = con.getInputStream();
                        int bytesRead = -1;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            System.out.println(new String(buffer, 0, bytesRead));
                        }
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (method.equals("PUT")) {
                String boundary = System.currentTimeMillis() + "";
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                BufferedOutputStream request1 = new BufferedOutputStream(con.getOutputStream());
                bufferOutFormData(request.getBody(), boundary, request1);
                System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                if (showResponseHeaders) {
                    System.out.println("headers: " + con.getHeaderFields());
                }
                System.out.println("Response Body:");
                if (saveOutput) {

                    InputStream inputStream = con.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(saveResponse);
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    outputStream.close();
                    inputStream.close();

                } else {
                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();
                }
            } else if (method.equals("DELETE")) {
                System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                if (showResponseHeaders) {
                    System.out.println("headers: " + con.getHeaderFields());
                }
                System.out.println("Response Body:");
                if (saveOutput) {

                    InputStream inputStream = con.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(saveResponse);
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    outputStream.close();
                    inputStream.close();

                } else {
                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            System.out.println("connection problem");
        }

    }

    public static void doRequest(HTTPRequest requ) {
        try {
            HttpURLConnection con = (HttpURLConnection) (requ.getUrl()).openConnection();
            con.setRequestMethod(requ.getMethod());
            String method = requ.getMethod();
            try {

                if (!method.equals("GET") && !method.equals("DELETE")) {
                    con.setDoOutput(true);
                }
                for (int j = 0; j < requ.getHeaders().size(); j++) {
                    for (String key : requ.getHeaders().keySet()) {
                        con.setRequestProperty(key, requ.getHeaders().get(key));
                    }
                }

                boolean showResponseHeaders = true;

                if (method.equals("GET")) {

                    System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                    if (showResponseHeaders) {
                        System.out.println("headers: " + con.getHeaderFields());
                    }
                    System.out.println("Response Body:");

                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();

                } else if (method.equals("POST")) {
                    try {
                        String boundary = System.currentTimeMillis() + "";
                        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                        BufferedOutputStream request1 = new BufferedOutputStream(con.getOutputStream());
                        bufferOutFormData(requ.getBody(), boundary, request1);
                        System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                        if (showResponseHeaders) {
                            System.out.println("headers: " + con.getHeaderFields());

                        }
                        System.out.println("Response Body:");
                        InputStream inputStream = con.getInputStream();
                        int bytesRead = -1;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            System.out.println(new String(buffer, 0, bytesRead));
                        }
                        inputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (method.equals("PUT")) {
                    String boundary = System.currentTimeMillis() + "";
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    BufferedOutputStream request1 = new BufferedOutputStream(con.getOutputStream());
                    bufferOutFormData(requ.getBody(), boundary, request1);
                    System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                    if (showResponseHeaders) {
                        System.out.println("headers: " + con.getHeaderFields());
                    }
                    System.out.println("Response Body:");

                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();

                } else if (method.equals("DELETE")) {
                    System.out.println("status: " + con.getResponseCode() + " " + con.getResponseMessage());
                    if (showResponseHeaders) {
                        System.out.println("headers: " + con.getHeaderFields());
                    }
                    System.out.println("Response Body:");

                    InputStream inputStream = con.getInputStream();
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        System.out.println(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();
                    System.out.println();
                    System.out.println("***************************************");
                }
            } catch (Exception e) {
                System.out.println("connection problem");
            }

        } catch (IOException e) {

        }
    }

    public static void main(String[] args) throws MalformedURLException, IOException, UnknownHostException {

        run(args);

    }
}
