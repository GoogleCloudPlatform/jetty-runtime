import org.eclipse.jetty.load.generator.profile.*

node('master') {

  def version = '0.0.1-SNAPSHOT'

  //stage 'Checkout'
  git url: 'https://github.com/olamy/jetty-load-generator-app.git'

  //stage 'Maven'
  // System Dependent Locations
  //def mvntool = tool name: 'maven3', type: 'hudson.tasks.Maven$MavenInstallation'
  //def jdktool = tool name: 'jdk8', type: 'hudson.model.JDK'

  // Environment
  //List mvnEnv = ["PATH+MVN=${mvntool}/bin", "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}/", "MAVEN_HOME=${mvntool}"]
  List mvnEnv = []
  mvnEnv.add("MAVEN_OPTS=-Xms256m -Xmx1024m -Djava.awt.headless=true")
  withEnv(mvnEnv) {
    timeout(15) {
      sh "mvn -B clean package"
    }
  }

  archive 'target/app-${version}.jar'

  dir ('target') {
    def jettyPort = 9090
    parallel firstBranch: {
      sh "java -jar app-${version}.jar --port=${jettyPort}"
    }, secondBranch: {
      //def port = readFile 'jetty.local.port'
      sleep 5
      def profile = new ResourceProfile(new Resource( "/jenkins",
                                                      new Resource( "/jenkins/job/pipeline-test/",
                                                                    new Resource( "/logo.gif" ),
                                                                    new Resource( "/spacer.png" )
                                                      ),
                                                      new Resource( "jenkins/job/foo/" ),
                                                      new Resource( "/script.js",
                                                                    new Resource( "/library.js" ),
                                                                    new Resource( "/morestuff.js" )
                                                      ),
                                                      new Resource( "/anotherScript.js" ),
                                                      new Resource( "/iframeContents.html" ),
                                                      new Resource( "/moreIframeContents.html" ),
                                                      new Resource( "/favicon.ico" )
      )
      );

      def transport = org.eclipse.jetty.load.generator.LoadGenerator.Transport.HTTP;

      def timeUnit = java.util.concurrent.TimeUnit.SECONDS;

      loadgenerator host: 'localhost', port: jettyPort, resourceProfile: profile, users: 1, transactionRate: 1, transport: transport, runningTime: 20, runningTimeUnit: timeUnit

      sh "curl http://localhost:${jettyPort}/stop"

    },
             failFast: true


  }







}