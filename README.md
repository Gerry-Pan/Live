# Live
Spring boot 2.1.2.RELEASE + Netty,reactive security,reactive mongodb,reactive websocket,reactor kafka

部署目录结构：

config----------文件夹
  application.yml
  global.properties
  log4j2.xml
lib----------文件夹
logs----------文件夹
live-1.0.jar

rem --spring.config.location指定的可以是绝对路径也可以是classpath中的路径，--cluster.node.id指定节点编号
java -Dloader.path=lib -jar live-1.0.jar --server.port=8080 --server.servlet.context-path=/live --cluster.node.id=0 --spring.config.location=file:config/application.yml,file:config/global.properties --logging.config=file:config/log4j2.xml
