package org.colos.ejss.model_elements.ReNoLabs;

import java.util.Set;

import org.opensourcephysics.tools.minijar.MiniJar;

public class PackageElements {

  /**
   * Default main for packing the library (invoke main("create_jar"))
   * @param args
   * @param args
   */
  static public void main(String[] args) {
    String commandLine = 
      " -o ../Ejs/distribution/bin/javascript/model_elements/ReNoLabs/ReNoLabs.jar " + // The output JAR file, to be located under in bin/extensions/model_elements
//      " -m .;../../ejs.jar;../../ejs_lib.jar;../../osp.jar org.colos.ejs.model_elements.ReNoLabs.DataReaderElement "+ // To display something and also for EJS to know the main class
      " -s ../ModelElements/bin " + // the location of the compiled classes
      " -c ../Ejs/distribution/bin/osp.jar " +  // Because this class uses standard OSP classes
      " -c ../Ejs/distribution/bin/ejs.jar " +  // Because this class uses EJS classes
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/socket.io-client-1.0.0.jar" +
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/engine.io-client-1.0.0.jar" +
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/json-20171018.jar" +
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/okhttp-3.9.1.jar" +
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/okio-1.13.0.jar" +
      " -s ../ModelElements/src/org/colos/ejss/model_elements/ReNoLabs/lib/socket.io-client-java/animal-sniffer-annotations-1.16.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/xmlrpc-common-3.1.3.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/xmlrpc-client.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/org-apache-ws-commons-util.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/org-apache-commons-logging.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/xmlrpc-server-3.1-sources.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/org.apache.commons.httpclient.jar" +
      //" org/apache/++"+
      " -x ../Ejs/distribution/bin/osp.jar -x ../Ejs/distribution/bin/ejs.jar -x ../Ejs/distribution/bin/ejs_lib.jar -x ++Thumbs.db"+ // do not include these classes, nor MAC OS X's _Thumbs.db files
      " org/colos/ejss/model_elements/ReNoLabs/++ "; // get ALL files under this directory and its dependencies (for class files)
    System.out.println ("Processing "+commandLine);
    MiniJar sj = new MiniJar(commandLine.split(" "));
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+commandLine+"\n");
  }

}
