package github.suzume.server;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.luaj.MitsuhaLua;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.JavaPackage;
import java.io.RandomAccessFile;
import java.io.FileInputStream;


/**
 * @Author 铃芽
 * @Date 2023/04/29 16:22
 * @Describe Http 服务器实现类
 */
public class HttpServer extends NanoHTTPD {
    public String hdir;
    //private Map<Integer,Globals> g_map = new HashMap<Integer,Globals>();

    //自动补充文件名
    public static final String[] file_name_fill = new String[]{
        "index.html",
        "index.htm",
        "index.lua",
    };

    private boolean fileListEnabled;

    public HttpServer(int port, String d) {
        super(port);
        hdir = d;
    }

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession s) {
        //Uri: "/(.*)"
        try {
            File f;
            f = new File(hdir + s.getUri());

            //无斜杠
            for (String fn : file_name_fill) {
                if (!f.isFile())
                    f = new File(hdir + s.getUri() + fn);
                else
                    break;
            }

            //加斜杠
            for (String fn : file_name_fill) {
                if (!f.isFile())
                    f = new File(hdir + s.getUri() + "/" + fn);
                else
                    break;
            }

            /*if(!f.exists())
             f = new File("/sdcard/server" + s.getUri() + "文件名");*/
            InputStream is = Util.open(f);
            if (is == null) {
                f = new File(hdir + s.getUri());
                //文件夹存在
                if (f.isDirectory() && fileListEnabled) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<!DOCTYPE html><html><head>");
                    sb.append("<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no' /><meta name='renderer' content='webkit' /><meta name='force-rendering' content='webkit' /><meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1' />");
                    sb.append("</head><body>文件列表:<br>");
                    File[] ls = f.listFiles();
                    sb.append("<a href=\"../\">../</a><br>");
                    for (File d : ls) {
                        if (d.isDirectory())
                            sb.append("<a href=\"" + d.getName() + "/\">" + d.getName() + "/</a><br>");
                        else
                            sb.append("<a href=\"" + d.getName() + "\">" + d.getName() + "</a><br>");
                    }
                    sb.append("</body></html>");
                    return newFixedLengthResponse(Response.Status.OK, MIME_HTML, sb.toString());
                }
                //文件(夹)不存在
                return super.serve(s);
                //return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found.");
            }

            //获取MIME
            String mime = getMineType(f.toString());

            //执行Lua
            if ("executable/lua".equals(mime)) {
                mime = MIME_HTML;
                /*int hash = f.hashCode();
                 Globals g = g_map.get(hash);
                 if (g == null) {*/
                final Globals g = MitsuhaLua.newInstance();
                /*g_map.put(hash, g);
                 }*/
                final StringBuilder htm = new StringBuilder();
                g.set("print", new OneArgFunction(){
                        @Override
                        //public Varargs invoke(Varargs args){
                        public LuaValue call(LuaValue arg) {
                            /*int len = args.narg();
                             int i = 0;
                             while (i < len) {*/
                            htm.append(arg.tojstring());
                            htm.append("<br/>");
                            /*i++;
                             }*/
                            return null;
                        }
                    });
                g.set("jpackage", new OneArgFunction(){
                        @Override
                        public LuaValue call(LuaValue arg) {
                            JavaPackage jp = new JavaPackage(arg.checkjstring());
                            g.set(arg.checkjstring(), jp);
                            return jp;
                        }
                    });
                g.jset("code", Response.Status.class);
                g.jset("input", s);
                g.jset("output", htm);

                Object re = g.loadfile(f.toString()).jcall();

                if (re instanceof Response.Status == false) {
                    re = Response.Status.OK;
                }

                return newFixedLengthResponse((Response.Status) re, mime, htm.toString());
            }

            //普通文件

            String isRange = s.getHeaders().get("range");

            //断点下载，主要用在音视频哦!

            //BUG太多了
            if (isRange != null) {
                try {
                    //RandomAccessFile raf = new RandomAccessFile(f.toString(), "r");

                    FileInputStream fi = new FileInputStream(f);

                    // Client requested a specific range of the file.
                    long rangeStart = 0;
                    long rangeEnd = 0;
                    String[] rangeValues = isRange.substring("bytes=".length()).split("-");
                    try {
                        rangeStart = Long.parseLong(rangeValues[0]);
                        if (rangeValues.length > 1) {
                            rangeEnd = Long.parseLong(rangeValues[1]);
                        } else {
                            rangeEnd = f.length() - 1;
                        }
                    } catch (NumberFormatException e) {}

                    fi.skip(rangeStart);

                    long newLength = rangeEnd - rangeStart + 1;
                    String responseRange = String.format("bytes %d-%d/%d", rangeStart, rangeEnd, f.length());

                    Response response =  /*newChunkedResponse(
                         Response.Status.PARTIAL_CONTENT,
                         mime,
                         //new FileInputStream(raf.getFD())
                         fi
                         );*/
                        newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fi, newLength);
                    response.addHeader("Content-Length", "" + newLength);
                    response.addHeader("Accept-Ranges", "bytes");
                    response.addHeader("Content-Range", responseRange);

                    return response;
                } finally {}
            }

            //真•普通文件

            return newFixedLengthResponse(Response.Status.OK, mime, is, is.available());
        } catch (Throwable e) {
            StringBuilder eri = new StringBuilder();

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            eri.append(result);

            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, eri.toString().trim());
        }
    }

    /*public Object doAssets(Context co, Globals L, String n, LuaValue arg) {
     try {
     //TODO
     byte[] c = FileUtil.readAll(co.getAssets().open(n));
     return MitsuhaLua.toJavaValue(L.load(c, "铃铛").call(arg), Object.class);
     } catch (Throwable e) {
     return e.getMessage();
     }
     }*/

    public void setFileListEnabled(boolean c) {
        fileListEnabled = c;
    }

    public static String getMineType(String f) {
        String n = f.substring(f.lastIndexOf("."));
        String m;
        switch (n) {
            case".jpg":
            case".png":
            case".jpeg":
            case".ico":
                m = "image/jpeg";
                break;
            case".html":
            case".htm":
                m = MIME_HTML;
                break;
            case ".txt":
            case ".log":
                m = MIME_PLAINTEXT;
                break;
            case".gif":
                m = "image/gif";
                break;
            case".js":
                m = "application/javascript";
                break;
            case".css":
                m = "text/css";
                break;
            case".lua":
                m = "executable/lua";
                break;
            case".mp3":
            case".m4a":
                m = "audio/mpeg";
                break;
            case".mp4":
                m = "video/mp4";
                break;
            case".mpeg":
                m = "video/mpeg";
                break;
            case".ts":
                m = "video/MP2T";
                break;
            case".m3u8":
                m = "application/x-mpegURL";
                break;
            default:
                m = "application/octet-stream";
        }
        return m;
    }

}

