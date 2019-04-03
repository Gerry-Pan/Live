# Live
Spring boot 2.1.2.RELEASE + Netty,reactive security,reactive mongodb,reactive websocket,reactor kafka<br>

部署目录结构：  

<ul>
  <li>
    config
    <ul>
      <li>application.yml</li>
      <li>global.properties</li>
      <li>log4j2.xml</li>
    </ul>
  </li>
  <li>
    lib
    <ul>
      <li>**.jar</li>
    </ul>
  </li>
  <li>
    logs
    <ul>
      <li>info.log</li>
      <li>error.log</li>
    </ul>
  </li>
  <li>live-1.0.jar</li>
</ul>

<code>java -Dloader.path=lib -jar live-1.0.jar --server.port=8080 --server.servlet.context-path=/live --cluster.node.id=0 --spring.config.location=file:config/application.yml,file:config/global.properties --logging.config=file:config/log4j2.xml</code>

注：--spring.config.location指定的可以是绝对路径也可以是classpath中的路径，--cluster.node.id指定节点编号  
