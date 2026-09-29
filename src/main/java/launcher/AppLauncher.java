package launcher;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class AppLauncher {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            port = Integer.parseInt(portEnv);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // triggers default connector creation

        String webappDirLocation = new File("src/main/webapp").getAbsolutePath();
        File additionWebInfClasses = new File("target/classes");
        ClassLoader parentClassLoader = AppLauncher.class.getClassLoader();

        // Context 1: /webapp_w5
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/webapp_w5", webappDirLocation);
        ctx.setParentClassLoader(parentClassLoader);
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        // Context 2: root /
        StandardContext rootCtx = (StandardContext) tomcat.addWebapp("", webappDirLocation);
        rootCtx.setParentClassLoader(parentClassLoader);
        WebResourceRoot rootResources = new StandardRoot(rootCtx);
        rootResources.addPreResources(new DirResourceSet(rootResources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        rootCtx.setResources(rootResources);

        System.out.println("=================================================");
        System.out.println(" SQL Gateway is starting on port " + port + "...");
        System.out.println(" URL: http://localhost:" + port + "/webapp_w5/");
        System.out.println(" URL: http://localhost:" + port + "/");
        System.out.println("=================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}
